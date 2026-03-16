package com.deliverysystem.service;

import com.deliverysystem.domain.Box;
import com.deliverysystem.domain.Order;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.CreateOrderRequest;
import com.deliverysystem.repository.BoxRepository;
import com.deliverysystem.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final BoxRepository boxRepository;
    private final PostcodeRoutingService postcodeRoutingService;
    private final AuditService auditService;
    
    public OrderService(OrderRepository orderRepository, BoxRepository boxRepository, PostcodeRoutingService postcodeRoutingService, AuditService auditService) {
        this.orderRepository = orderRepository;
        this.boxRepository = boxRepository;
        this.postcodeRoutingService = postcodeRoutingService;
        this.auditService = auditService;
    }
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Transactional
    public Order createOrder(CreateOrderRequest request, User user) {
        // Check for duplicate order
        Optional<Order> existingOrder = orderRepository.findByOrderIdAndDespatchId(
            request.getOrderId(), request.getDespatchId());
        
        if (existingOrder.isPresent()) {
            throw new IllegalArgumentException(
                String.format("Order with Order ID %s and Despatch ID %s already exists", 
                    request.getOrderId(), request.getDespatchId()));
        }
        
        // Resolve postcode to route
        Route route = postcodeRoutingService.resolvePostcodeToRoute(
            request.getDeliveryPostcode(), LocalDate.now())
            .orElseThrow(() -> new IllegalStateException(
                "Could not resolve postcode " + request.getDeliveryPostcode() + " to a route"));
        
        // Create order
        Order order = new Order();
        order.setOrderId(request.getOrderId());
        order.setDespatchId(request.getDespatchId());
        order.setCustomerAddress(request.getCustomerAddress());
        order.setDeliveryPostcode(request.getDeliveryPostcode());
        order.setRoute(route);
        order.setStatus("PENDING");
        
        if (request.getOrderDate() != null) {
            order.setOrderDate(LocalDate.parse(request.getOrderDate(), DATE_FORMATTER));
        }
        if (request.getRequestedDeliveryDate() != null) {
            order.setRequestedDeliveryDate(LocalDate.parse(request.getRequestedDeliveryDate(), DATE_FORMATTER));
        }
        
        order = orderRepository.save(order);
        
        // Create boxes
        int expectedBoxCount = request.getExpectedBoxCount() != null ? 
            request.getExpectedBoxCount() : 
            (request.getBoxIdentifiers() != null ? request.getBoxIdentifiers().size() : 0);
        
        List<Box> boxes = new ArrayList<>();
        if (request.getBoxIdentifiers() != null) {
            for (String identifier : request.getBoxIdentifiers()) {
                Box box = new Box();
                box.setOrder(order);
                box.setIdentifier(identifier);
                box.setStatus(Box.BoxStatus.EXPECTED);
                boxes.add(box);
            }
        } else {
            // Create expected boxes without identifiers
            for (int i = 0; i < expectedBoxCount; i++) {
                Box box = new Box();
                box.setOrder(order);
                box.setStatus(Box.BoxStatus.EXPECTED);
                boxes.add(box);
            }
        }
        
        boxRepository.saveAll(boxes);
        
        // Audit
        String depotId = route.getDepot() != null ? route.getDepot().getId() : null;
        auditService.logCreate(user, "Order", order.getId(), depotId, order);
        
        log.info("Created order {} with {} boxes, allocated to route {}", 
            order.getId(), boxes.size(), route.getId());
        
        return order;
    }
    
    @Transactional
    public Box receiveBox(String boxIdOrIdentifier, User user) {
        // Try to find by UUID first, then by identifier
        Box box = boxRepository.findById(boxIdOrIdentifier)
            .orElseGet(() -> boxRepository.findByIdentifier(boxIdOrIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Box not found: " + boxIdOrIdentifier)));
        
        if (box.getStatus() == Box.BoxStatus.RECEIVED) {
            throw new IllegalStateException("Box " + boxIdOrIdentifier + " has already been received");
        }
        
        box.setStatus(Box.BoxStatus.RECEIVED);
        box.setReceivedAt(java.time.LocalDateTime.now());
        box = boxRepository.save(box);
        
        // Audit
        String depotId = box.getOrder().getRoute() != null && box.getOrder().getRoute().getDepot() != null ?
            box.getOrder().getRoute().getDepot().getId() : null;
        auditService.logUpdate(user, "Box", box.getId(), depotId, 
            Box.BoxStatus.EXPECTED, Box.BoxStatus.RECEIVED);
        
        log.info("Box {} (identifier: {}) received for order {}", 
            box.getId(), box.getIdentifier() != null ? box.getIdentifier() : "N/A", box.getOrder().getId());
        
        return box;
    }
    
    @Transactional
    public Order flagException(String orderId, String reason, User user) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        String previousStatus = order.getStatus();
        order.setStatus("EXCEPTION");
        order = orderRepository.save(order);
        
        // Audit
        String depotId = order.getRoute() != null && order.getRoute().getDepot() != null ?
            order.getRoute().getDepot().getId() : null;
        auditService.logUpdate(user, "Order", order.getId(), depotId, previousStatus, "EXCEPTION: " + reason);
        
        log.info("Order {} flagged with exception: {}", orderId, reason);
        
        return order;
    }
    
    @Transactional
    public Order markReadyForManifest(String orderId, User user) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        String previousStatus = order.getStatus();
        order.setStatus("READY_FOR_MANIFEST");
        order = orderRepository.save(order);
        
        // Audit
        String depotId = order.getRoute() != null && order.getRoute().getDepot() != null ?
            order.getRoute().getDepot().getId() : null;
        auditService.logUpdate(user, "Order", order.getId(), depotId, previousStatus, "READY_FOR_MANIFEST");
        
        log.info("Order {} marked as ready for manifest", orderId);
        
        return order;
    }
}

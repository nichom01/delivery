package com.deliverysystem.service;

import com.deliverysystem.domain.*;
import com.deliverysystem.dto.*;
import com.deliverysystem.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {
    
    private static final Logger log = LoggerFactory.getLogger(RouteService.class);
    
    private final RouteRepository routeRepository;
    private final ManifestRepository manifestRepository;
    private final BoxRepository boxRepository;
    private final DepotRepository depotRepository;
    private final AuditService auditService;
    
    public RouteService(
            RouteRepository routeRepository,
            ManifestRepository manifestRepository,
            BoxRepository boxRepository,
            DepotRepository depotRepository,
            AuditService auditService) {
        this.routeRepository = routeRepository;
        this.manifestRepository = manifestRepository;
        this.boxRepository = boxRepository;
        this.depotRepository = depotRepository;
        this.auditService = auditService;
    }
    
    @Transactional(readOnly = true)
    public RouteDrilldownDto getRouteDrilldown(String routeId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));
        
        RouteDrilldownDto dto = new RouteDrilldownDto();
        dto.setRouteName(route.getName());
        
        // Get all received boxes for the route before checking for manifests
        List<Box> routeBoxes = boxRepository.findByOrderRouteIdReceived(routeId);
        
        // Get manifest for the date
        List<Manifest> manifests = manifestRepository.findManifestsByRouteIdAndDate(routeId, date);
        
        if (manifests.isEmpty()) {
            dto.setVehicle("-");
            dto.setDriver("-");
            // Use route boxes for stats and stops instead of empty
            RouteStatsDto stats = calculateRouteStats(routeBoxes);
            dto.setStats(stats);
            List<DeliveryStopDto> stops = createDeliveryStops(routeBoxes);
            dto.setStops(stops);
            return dto;
        }
        
        Manifest manifest = manifests.get(0);
        dto.setVehicle(manifest.getVehicle().getRegistration());
        dto.setDriver(manifest.getDriver().getName());
        
        // Use route boxes for stats and stops instead of just manifest boxes
        RouteStatsDto stats = calculateRouteStats(routeBoxes);
        dto.setStats(stats);
        
        // Create delivery stops from route boxes grouped by order
        List<DeliveryStopDto> stops = createDeliveryStops(routeBoxes);
        dto.setStops(stops);
        
        return dto;
    }
    
    private RouteStatsDto calculateRouteStats(List<Box> boxes) {
        int deliveriesTotal = (int) boxes.stream()
            .map(b -> b.getOrder().getId())
            .distinct()
            .count();
        
        int deliveriesDone = (int) boxes.stream()
            .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
            .map(b -> b.getOrder().getId())
            .distinct()
            .count();
        
        int boxesTotal = boxes.size();
        int boxesDone = (int) boxes.stream()
            .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
            .count();
        
        int exceptionsCount = (int) boxes.stream()
            .filter(b -> b.getStatus() == Box.BoxStatus.EXCEPTION)
            .count();
        
        // Find last activity
        String lastActivity = null;
        String lastActivityPostcode = null;
        LocalDateTime lastActivityTime = null;
        
        for (Box box : boxes) {
            if (box.getStatus() == Box.BoxStatus.DELIVERED && box.getReceivedAt() != null) {
                if (lastActivityTime == null || box.getReceivedAt().isAfter(lastActivityTime)) {
                    lastActivityTime = box.getReceivedAt();
                    lastActivity = box.getReceivedAt().toString();
                    lastActivityPostcode = box.getOrder().getDeliveryPostcode();
                }
            }
        }
        
        return new RouteStatsDto(
            deliveriesDone,
            deliveriesTotal,
            boxesDone,
            boxesTotal,
            exceptionsCount,
            lastActivity,
            lastActivityPostcode
        );
    }
    
    private List<DeliveryStopDto> createDeliveryStops(List<Box> boxes) {
        // Group boxes by order
        List<DeliveryStopDto> stops = boxes.stream()
            .collect(Collectors.groupingBy(Box::getOrder))
            .entrySet()
            .stream()
            .map(entry -> {
                Order order = entry.getKey();
                List<Box> orderBoxes = entry.getValue();
                
                DeliveryStopDto stop = new DeliveryStopDto();
                stop.setAddress(order.getCustomerAddress() != null ? order.getCustomerAddress() : "Unknown");
                stop.setPostcode(order.getDeliveryPostcode());
                
                // Determine boxes count/status
                int deliveredCount = (int) orderBoxes.stream()
                    .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
                    .count();
                int totalCount = orderBoxes.size();
                
                if (deliveredCount == totalCount) {
                    stop.setBoxes(totalCount);
                    stop.setStatus("Delivered");
                } else if (deliveredCount > 0) {
                    stop.setBoxes(deliveredCount + " of " + totalCount);
                    stop.setStatus("Partial");
                } else {
                    stop.setBoxes(totalCount);
                    stop.setStatus("Pending");
                }
                
                // Set delivery time if any box is delivered
                LocalDateTime deliveryTime = orderBoxes.stream()
                    .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED && b.getReceivedAt() != null)
                    .map(Box::getReceivedAt)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
                
                if (deliveryTime != null) {
                    stop.setDeliveryTime(deliveryTime.toString());
                }
                
                // Check for POD (Proof of Delivery) - simplified check
                stop.setHasPod(deliveredCount > 0);
                
                return stop;
            })
            .sorted(Comparator.comparing(DeliveryStopDto::getPostcode))
            .collect(Collectors.toList());
        
        // Add sequence numbers
        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).setSeq(i + 1);
        }
        
        return stops;
    }
    
    @Transactional
    public Route createRoute(String code, String name, String description, String depotId, User user) {
        Depot depot = depotRepository.findById(depotId)
            .orElseThrow(() -> new IllegalArgumentException("Depot not found: " + depotId));
        
        Route route = new Route();
        route.setCode(code);
        route.setName(name);
        route.setDescription(description);
        route.setDepot(depot);
        
        route = routeRepository.save(route);
        
        // Audit
        auditService.logCreate(user, "Route", route.getId(), depotId, route);
        
        log.info("Created route {} ({}) for depot {}", route.getCode(), route.getName(), depotId);
        
        return route;
    }
    
    @Transactional
    public Route updateRoute(String routeId, String name, String description, User user) {
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));
        
        String previousName = route.getName();
        String previousDescription = route.getDescription();
        
        if (name != null && !name.isEmpty()) {
            route.setName(name);
        }
        if (description != null) {
            route.setDescription(description);
        }
        
        route = routeRepository.save(route);
        
        // Audit
        String depotId = route.getDepot() != null ? route.getDepot().getId() : null;
        String beforeValue = String.format("Name: %s, Description: %s", previousName, previousDescription);
        String afterValue = String.format("Name: %s, Description: %s", route.getName(), route.getDescription());
        auditService.logUpdate(user, "Route", route.getId(), depotId, beforeValue, afterValue);
        
        log.info("Updated route {}", routeId);
        
        return route;
    }
}

package com.deliverysystem.service;

import com.deliverysystem.domain.*;
import com.deliverysystem.dto.*;
import com.deliverysystem.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    
    private final DepotRepository depotRepository;
    private final RouteRepository routeRepository;
    private final ManifestRepository manifestRepository;
    private final OrderRepository orderRepository;
    private final BoxRepository boxRepository;
    
    public DashboardService(
            DepotRepository depotRepository,
            RouteRepository routeRepository,
            ManifestRepository manifestRepository,
            OrderRepository orderRepository,
            BoxRepository boxRepository) {
        this.depotRepository = depotRepository;
        this.routeRepository = routeRepository;
        this.manifestRepository = manifestRepository;
        this.orderRepository = orderRepository;
        this.boxRepository = boxRepository;
    }
    
    @Transactional(readOnly = true)
    public DashboardDto getDashboard(String depotId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        List<Route> routes;
        if (depotId != null && !depotId.isEmpty()) {
            Depot depot = depotRepository.findById(depotId).orElse(null);
            if (depot == null) {
                log.warn("Depot not found: {}", depotId);
                return createEmptyDashboard(date);
            }
            routes = routeRepository.findByDepotId(depotId);
        } else {
            routes = routeRepository.findAll();
        }
        
        DashboardDto dashboard = new DashboardDto();
        dashboard.setDate(date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Calculate summary
        DashboardSummaryDto summary = calculateSummary(routes, date);
        dashboard.setSummary(summary);
        
        // Calculate route summaries
        List<RouteSummaryDto> routeSummaries = calculateRouteSummaries(routes, date);
        dashboard.setRouteSummary(routeSummaries);
        
        // Get exceptions (orders with boxes in EXCEPTION status)
        List<ExceptionDto> exceptions = getExceptions(depotId);
        dashboard.setOpenExceptions(exceptions);
        
        // Get orders awaiting goods
        List<OrderAwaitingGoodsDto> awaitingGoods = getOrdersAwaitingGoods(depotId);
        dashboard.setAwaitingGoods(awaitingGoods);
        
        return dashboard;
    }
    
    private DashboardSummaryDto calculateSummary(List<Route> routes, LocalDate date) {
        int totalRoutes = routes.size();
        int deliveriesComplete = 0;
        int deliveriesTotal = 0;
        int boxesDelivered = 0;
        int boxesTotal = 0;
        int exceptionsCount = 0;
        
        for (Route route : routes) {
            List<Manifest> manifests = manifestRepository.findManifestsByRouteIdAndDate(route.getId(), date);
            
            for (Manifest manifest : manifests) {
                List<Box> boxes = boxRepository.findByManifestId(manifest.getId());
                if (boxes.isEmpty()) {
                    continue;
                }
                
                int routeBoxesTotal = boxes.size();
                int routeBoxesDelivered = (int) boxes.stream()
                    .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
                    .count();
                
                boxesTotal += routeBoxesTotal;
                boxesDelivered += routeBoxesDelivered;
                
                // Count unique orders
                long uniqueOrders = boxes.stream()
                    .map(b -> b.getOrder().getId())
                    .distinct()
                    .count();
                
                deliveriesTotal += uniqueOrders;
                
                if (manifest.getStatus() == Manifest.ManifestStatus.COMPLETE) {
                    deliveriesComplete += uniqueOrders;
                }
            }
            
            // Count exceptions for this route
            List<Box> exceptionBoxes = boxRepository.findByOrderRouteIdAndStatus(route.getId(), Box.BoxStatus.EXCEPTION);
            exceptionsCount += exceptionBoxes.size();
        }
        
        return new DashboardSummaryDto(
            totalRoutes,
            deliveriesComplete,
            deliveriesTotal,
            boxesDelivered,
            boxesTotal,
            exceptionsCount
        );
    }
    
    private List<RouteSummaryDto> calculateRouteSummaries(List<Route> routes, LocalDate date) {
        List<RouteSummaryDto> summaries = new ArrayList<>();
        
        for (Route route : routes) {
            List<Manifest> manifests = manifestRepository.findManifestsByRouteIdAndDate(route.getId(), date);
            
            RouteSummaryDto summary = new RouteSummaryDto();
            summary.setRouteId(route.getId());
            summary.setRouteName(route.getName());
            summary.setDescription(route.getDescription() != null ? route.getDescription() : "");
            
            if (!manifests.isEmpty()) {
                Manifest manifest = manifests.get(0); // Use first manifest for the date
                summary.setVehicle(manifest.getVehicle().getRegistration());
                summary.setDriver(manifest.getDriver().getName());
                
                List<Box> boxes = boxRepository.findByManifestId(manifest.getId());
                int boxesTotal = boxes.size();
                int boxesDone = (int) boxes.stream()
                    .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
                    .count();
                
                summary.setBoxesTotal(boxesTotal);
                summary.setBoxesDone(boxesDone);
                
                // Count unique orders
                long uniqueOrders = boxes.stream()
                    .map(b -> b.getOrder().getId())
                    .distinct()
                    .count();
                
                long deliveredOrders = boxes.stream()
                    .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
                    .map(b -> b.getOrder().getId())
                    .distinct()
                    .count();
                
                summary.setDeliveriesTotal((int) uniqueOrders);
                summary.setDeliveriesDone((int) deliveredOrders);
                
                // Determine status
                if (manifest.getStatus() == Manifest.ManifestStatus.COMPLETE) {
                    summary.setStatus("Complete");
                } else if (manifest.getStatus() == Manifest.ManifestStatus.IN_PROGRESS) {
                    summary.setStatus("In Progress");
                    int progressPercent = boxesTotal > 0 ? (boxesDone * 100 / boxesTotal) : 0;
                    summary.setProgressNote(progressPercent + "%");
                } else if (manifest.getStatus() == Manifest.ManifestStatus.CONFIRMED) {
                    summary.setStatus("Departed");
                    summary.setProgressNote("Departed " + java.time.LocalTime.now().toString().substring(0, 5));
                } else {
                    summary.setStatus("Pending");
                    summary.setProgressNote("Awaiting manifest");
                }
            } else {
                summary.setVehicle("-");
                summary.setDriver("-");
                summary.setDeliveriesDone(0);
                summary.setDeliveriesTotal(0);
                summary.setBoxesDone(0);
                summary.setBoxesTotal(0);
                summary.setStatus("Pending");
                summary.setProgressNote("Awaiting manifest");
            }
            
            summaries.add(summary);
        }
        
        return summaries;
    }
    
    private List<ExceptionDto> getExceptions(String depotId) {
        List<Box> exceptionBoxes;
        if (depotId != null && !depotId.isEmpty()) {
            exceptionBoxes = boxRepository.findByOrderDepotIdAndStatus(depotId, Box.BoxStatus.EXCEPTION);
        } else {
            exceptionBoxes = boxRepository.findByStatus(Box.BoxStatus.EXCEPTION);
        }
        
        return exceptionBoxes.stream()
            .collect(Collectors.groupingBy(b -> b.getOrder().getOrderId()))
            .entrySet()
            .stream()
            .map(entry -> {
                ExceptionDto dto = new ExceptionDto();
                dto.setOrderId(entry.getKey());
                List<Box> boxes = entry.getValue();
                int totalBoxes = boxes.size();
                int exceptionBoxesCount = boxes.size();
                dto.setBoxesSummary(exceptionBoxesCount + " of " + totalBoxes);
                if (boxes.get(0).getOrder().getRoute() != null) {
                    dto.setRouteName(boxes.get(0).getOrder().getRoute().getName());
                } else {
                    dto.setRouteName("-");
                }
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    public List<OrderAwaitingGoodsDto> getOrdersAwaitingGoods(String depotId) {
        List<Order> orders;
        if (depotId != null && !depotId.isEmpty()) {
            orders = orderRepository.findByDepotId(depotId);
        } else {
            orders = orderRepository.findAll();
        }
        
        List<OrderAwaitingGoodsDto> result = new ArrayList<>();
        
        for (Order order : orders) {
            List<Box> boxes = boxRepository.findByOrderId(order.getId());
            long receivedCount = boxes.stream()
                .filter(b -> b.getStatus() == Box.BoxStatus.RECEIVED || b.getStatus() == Box.BoxStatus.MANIFESTED)
                .count();
            long expectedCount = boxes.stream()
                .filter(b -> b.getStatus() == Box.BoxStatus.EXPECTED)
                .count();
            
            if (expectedCount > 0 || receivedCount < boxes.size()) {
                OrderAwaitingGoodsDto dto = new OrderAwaitingGoodsDto();
                dto.setOrderId(order.getOrderId());
                dto.setCustomer(order.getCustomerAddress() != null ? order.getCustomerAddress() : "Unknown");
                dto.setRouteName(order.getRoute() != null ? order.getRoute().getName() : "-");
                dto.setBoxesReceived((int) receivedCount);
                dto.setBoxesExpected((int) (expectedCount + boxes.size() - receivedCount));
                
                List<BoxDto> boxDtos = boxes.stream()
                    .map(b -> {
                        BoxDto boxDto = new BoxDto();
                        boxDto.setId(b.getId());
                        String status = b.getStatus().name().toLowerCase();
                        if (status.equals("received") || status.equals("manifested")) {
                            boxDto.setStatus("received");
                        } else if (status.equals("expected")) {
                            boxDto.setStatus("pending");
                        } else {
                            boxDto.setStatus("missing");
                        }
                        if (b.getReceivedAt() != null) {
                            boxDto.setReceivedAt(b.getReceivedAt().toString());
                        }
                        return boxDto;
                    })
                    .collect(Collectors.toList());
                
                dto.setBoxes(boxDtos);
                result.add(dto);
            }
        }
        
        return result;
    }
    
    private DashboardDto createEmptyDashboard(LocalDate date) {
        DashboardDto dashboard = new DashboardDto();
        dashboard.setDate(date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
        dashboard.setSummary(new DashboardSummaryDto(0, 0, 0, 0, 0, 0));
        dashboard.setRouteSummary(new ArrayList<>());
        dashboard.setOpenExceptions(new ArrayList<>());
        dashboard.setAwaitingGoods(new ArrayList<>());
        return dashboard;
    }
}

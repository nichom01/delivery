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
import java.util.Optional;
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
            // Get all received boxes for the route before checking for manifests
            List<Box> routeBoxes = boxRepository.findByOrderRouteIdReceived(route.getId());

            List<Manifest> manifests = manifestRepository.findManifestsByRouteIdAndDate(route.getId(), date);

            RouteSummaryDto summary = new RouteSummaryDto();
            summary.setRouteId(route.getId());
            summary.setRouteName(route.getName());
            summary.setDescription(route.getDescription() != null ? route.getDescription() : "");

            if (!manifests.isEmpty()) {
                Manifest manifest = manifests.get(0); // Use first manifest for the date
                summary.setVehicle(manifest.getVehicle().getRegistration());
                summary.setDriver(manifest.getDriver().getName());

                // Use route boxes for totals instead of just manifest boxes
                int boxesTotal = routeBoxes.size();
                int boxesDone = (int) routeBoxes.stream()
                    .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
                    .count();

                summary.setBoxesTotal(boxesTotal);
                summary.setBoxesDone(boxesDone);

                // Count unique orders from route boxes
                long uniqueOrders = routeBoxes.stream()
                    .map(b -> b.getOrder().getId())
                    .distinct()
                    .count();

                long deliveredOrders = routeBoxes.stream()
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
                // No manifest exists - show received boxes with status "Ready to manifest"
                summary.setVehicle("-");
                summary.setDriver("-");

                if (!routeBoxes.isEmpty()) {
                    int boxesTotal = routeBoxes.size();
                    int boxesDone = (int) routeBoxes.stream()
                        .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
                        .count();

                    summary.setBoxesTotal(boxesTotal);
                    summary.setBoxesDone(boxesDone);

                    // Count unique orders from route boxes
                    long uniqueOrders = routeBoxes.stream()
                        .map(b -> b.getOrder().getId())
                        .distinct()
                        .count();

                    long deliveredOrders = routeBoxes.stream()
                        .filter(b -> b.getStatus() == Box.BoxStatus.DELIVERED)
                        .map(b -> b.getOrder().getId())
                        .distinct()
                        .count();

                    summary.setDeliveriesTotal((int) uniqueOrders);
                    summary.setDeliveriesDone((int) deliveredOrders);
                    summary.setStatus("Ready to manifest");
                    summary.setProgressNote("");
                } else {
                    summary.setDeliveriesDone(0);
                    summary.setDeliveriesTotal(0);
                    summary.setBoxesDone(0);
                    summary.setBoxesTotal(0);
                    summary.setStatus("Pending");
                    summary.setProgressNote("Awaiting manifest");
                }
            }

            // Populate planned-payload figures from requestedDeliveryDate (all box statuses)
            List<Order> plannedOrders = orderRepository.findByRouteIdAndRequestedDeliveryDate(route.getId(), date);
            int plannedOrderCount = plannedOrders.size();
            int plannedBoxCount = 0;
            int awaitingGoodsOrderCount = 0;
            int awaitingGoodsBoxCount = 0;

            for (Order order : plannedOrders) {
                List<Box> boxes = boxRepository.findByOrderId(order.getId());
                plannedBoxCount += boxes.size();
                long expectedInOrder = boxes.stream()
                    .filter(b -> b.getStatus() == Box.BoxStatus.EXPECTED)
                    .count();
                if (expectedInOrder > 0) {
                    awaitingGoodsOrderCount++;
                    awaitingGoodsBoxCount += (int) expectedInOrder;
                }
            }

            summary.setPlannedOrders(plannedOrderCount);
            summary.setPlannedBoxes(plannedBoxCount);
            summary.setAwaitingGoodsOrders(awaitingGoodsOrderCount);
            summary.setAwaitingGoodsBoxes(awaitingGoodsBoxCount);

            summaries.add(summary);
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    public DayPlanDto getDayPlan(String depotId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new IllegalArgumentException("Depot not found: " + depotId));

        List<Route> routes = routeRepository.findByDepotId(depotId);

        DayPlanDto dayPlan = new DayPlanDto();
        dayPlan.setDate(date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));
        dayPlan.setDepotId(depot.getId());
        dayPlan.setDepotName(depot.getName());

        int depotTotalOrders = 0;
        int depotTotalBoxes = 0;
        List<DayPlanRouteDto> routeDtos = new ArrayList<>();

        for (Route route : routes) {
            List<Order> plannedOrders = orderRepository.findByRouteIdAndRequestedDeliveryDate(route.getId(), date);

            DayPlanRouteDto routeDto = new DayPlanRouteDto();
            routeDto.setRouteId(route.getId());
            routeDto.setRouteCode(route.getCode());
            routeDto.setRouteName(route.getName());

            // Manifest for this route+date (for vehicle/driver/status)
            Optional<Manifest> manifestOpt = manifestRepository.findByRouteIdAndDate(route.getId(), date);
            if (manifestOpt.isPresent()) {
                Manifest manifest = manifestOpt.get();
                routeDto.setManifestStatus(manifest.getStatus().name());
                routeDto.setVehicleRegistration(manifest.getVehicle().getRegistration());
                routeDto.setDriverName(manifest.getDriver().getName());
            } else {
                routeDto.setManifestStatus("NONE");
                routeDto.setVehicleRegistration(null);
                routeDto.setDriverName(null);
            }

            int routeTotalBoxes = 0;
            int fullyReceived = 0;
            int partiallyReceived = 0;
            int notYetReceived = 0;

            List<DayPlanOrderDto> orderDtos = new ArrayList<>();
            for (Order order : plannedOrders) {
                List<Box> boxes = boxRepository.findByOrderId(order.getId());

                int totalBoxes = boxes.size();
                int expectedBoxes = (int) boxes.stream()
                        .filter(b -> b.getStatus() == Box.BoxStatus.EXPECTED).count();
                int receivedBoxes = (int) boxes.stream()
                        .filter(b -> b.getStatus() == Box.BoxStatus.RECEIVED).count();
                int readyBoxes = (int) boxes.stream()
                        .filter(b -> b.getStatus() == Box.BoxStatus.MANIFESTED
                                || b.getStatus() == Box.BoxStatus.DELIVERED).count();

                DayPlanOrderDto orderDto = new DayPlanOrderDto();
                orderDto.setId(order.getId());
                orderDto.setOrderId(order.getOrderId());
                orderDto.setCustomerAddress(order.getCustomerAddress() != null ? order.getCustomerAddress() : "");
                orderDto.setDeliveryPostcode(order.getDeliveryPostcode());
                orderDto.setOrderStatus(order.getStatus());
                orderDto.setTotalBoxes(totalBoxes);
                orderDto.setBoxesExpected(expectedBoxes);
                orderDto.setBoxesReceived(receivedBoxes);
                orderDto.setBoxesReady(readyBoxes);
                orderDtos.add(orderDto);

                routeTotalBoxes += totalBoxes;

                if (expectedBoxes == 0) {
                    fullyReceived++;
                } else if (expectedBoxes < totalBoxes) {
                    partiallyReceived++;
                } else {
                    notYetReceived++;
                }
            }

            // Sort orders by postcode for consistent presentation
            orderDtos.sort(java.util.Comparator.comparing(DayPlanOrderDto::getDeliveryPostcode));

            routeDto.setTotalOrders(plannedOrders.size());
            routeDto.setTotalBoxes(routeTotalBoxes);
            routeDto.setOrdersFullyReceived(fullyReceived);
            routeDto.setOrdersPartiallyReceived(partiallyReceived);
            routeDto.setOrdersNotYetReceived(notYetReceived);
            routeDto.setOrders(orderDtos);

            depotTotalOrders += plannedOrders.size();
            depotTotalBoxes += routeTotalBoxes;
            routeDtos.add(routeDto);
        }

        dayPlan.setTotalOrdersOnDay(depotTotalOrders);
        dayPlan.setTotalBoxesOnDay(depotTotalBoxes);
        dayPlan.setRoutes(routeDtos);

        return dayPlan;
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

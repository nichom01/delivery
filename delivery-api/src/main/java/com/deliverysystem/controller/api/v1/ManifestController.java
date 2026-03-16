package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Box;
import com.deliverysystem.domain.Manifest;
import com.deliverysystem.domain.Order;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreateManifestRequest;
import com.deliverysystem.dto.ManifestDto;
import com.deliverysystem.dto.ManifestStopDto;
import com.deliverysystem.dto.UpdateManifestRequest;
import com.deliverysystem.repository.BoxRepository;
import com.deliverysystem.repository.ManifestRepository;
import com.deliverysystem.repository.OrderRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.ManifestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/manifests")
public class ManifestController {
    
    private final ManifestService manifestService;
    private final ManifestRepository manifestRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final OrderRepository orderRepository;
    private final BoxRepository boxRepository;
    
    public ManifestController(ManifestService manifestService, ManifestRepository manifestRepository, UserRepository userRepository, JwtTokenProvider tokenProvider, OrderRepository orderRepository, BoxRepository boxRepository) {
        this.manifestService = manifestService;
        this.manifestRepository = manifestRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.orderRepository = orderRepository;
        this.boxRepository = boxRepository;
    }
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ManifestDto>>> getManifests(
            @RequestParam(required = false) String depotId,
            @RequestParam(required = false) String date) {
        List<Manifest> manifests;
        if (depotId != null && date != null) {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            manifests = manifestRepository.findByRouteDepotIdAndDate(depotId, localDate);
        } else if (depotId != null) {
            manifests = manifestRepository.findByRouteDepotId(depotId);
        } else {
            manifests = manifestRepository.findAll();
        }
        
        List<ManifestDto> dtos = manifests.stream().map(this::toDto).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
    
    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<ApiResponse<List<ManifestStopDto>>> getRouteStops(
            @PathVariable String routeId) {
        List<ManifestStopDto> stops = populateRouteStops(routeId);
        return ResponseEntity.ok(ApiResponse.success(stops));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ManifestDto>> createManifest(
            @Valid @RequestBody CreateManifestRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Manifest manifest = manifestService.createManifest(request, user);
        return ResponseEntity.ok(ApiResponse.success("Manifest created", toDto(manifest)));
    }
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ManifestDto>> confirmManifest(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Manifest manifest = manifestService.confirmManifest(id, user);
        return ResponseEntity.ok(ApiResponse.success("Manifest confirmed", toDto(manifest)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ManifestDto>> updateManifest(
            @PathVariable String id,
            @Valid @RequestBody UpdateManifestRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Manifest manifest = manifestService.updateManifest(
            id, request.getDriverId(), request.getVehicleId(), request.getDate(), user);
        return ResponseEntity.ok(ApiResponse.success("Manifest updated", toDto(manifest)));
    }
    
    @DeleteMapping("/{id}/stops/{orderId}")
    public ResponseEntity<ApiResponse<ManifestDto>> removeStopFromManifest(
            @PathVariable String id,
            @PathVariable String orderId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Manifest manifest = manifestService.removeStopFromManifest(id, orderId, user);
        return ResponseEntity.ok(ApiResponse.success("Stop removed from manifest", toDto(manifest)));
    }
    
    private ManifestDto toDto(Manifest manifest) {
        ManifestDto dto = new ManifestDto();
        dto.setId(manifest.getId());
        dto.setRouteId(manifest.getRoute().getId());
        dto.setDate(manifest.getDate().format(DATE_FORMATTER));
        dto.setDriverId(manifest.getDriver().getId());
        dto.setVehicleId(manifest.getVehicle().getId());
        dto.setStatus(manifest.getStatus().name());
        
        // Populate stops from orders with received boxes for this route
        List<ManifestStopDto> stops = populateManifestStops(manifest);
        dto.setStops(stops);
        
        return dto;
    }
    
    /**
     * Populate manifest stops from orders on the route that have received boxes.
     * For DRAFT manifests, shows all orders with received boxes.
     * For CONFIRMED manifests, shows orders with boxes assigned to this manifest.
     */
    private List<ManifestStopDto> populateManifestStops(Manifest manifest) {
        String routeId = manifest.getRoute().getId();
        return populateRouteStops(routeId, manifest.getId());
    }
    
    /**
     * Populate stops for a route (used when no manifest exists or for route-specific queries).
     * Shows all orders with received boxes for the route.
     */
    private List<ManifestStopDto> populateRouteStops(String routeId) {
        return populateRouteStops(routeId, null);
    }
    
    /**
     * Populate stops for a route, optionally filtering by manifest assignment.
     * Uses the same data source as dashboard (findByOrderRouteIdReceived) for consistency.
     */
    private List<ManifestStopDto> populateRouteStops(String routeId, String manifestId) {
        // Get all received/manifested/delivered boxes for the route (same as dashboard)
        List<Box> routeBoxes = boxRepository.findByOrderRouteIdReceived(routeId);
        
        // Group boxes by order
        Map<Order, List<Box>> boxesByOrder = routeBoxes.stream()
            .collect(Collectors.groupingBy(Box::getOrder));
        
        List<ManifestStopDto> stops = new ArrayList<>();
        
        for (Map.Entry<Order, List<Box>> entry : boxesByOrder.entrySet()) {
            Order order = entry.getKey();
            List<Box> orderBoxes = entry.getValue();
            
            // Filter out DELIVERED boxes for manifest building (only count RECEIVED and MANIFESTED)
            long receivedCount = orderBoxes.stream()
                .filter(b -> b.getStatus() == Box.BoxStatus.RECEIVED || b.getStatus() == Box.BoxStatus.MANIFESTED)
                .count();
            
            long manifestedCount = manifestId != null ? orderBoxes.stream()
                .filter(b -> b.getManifest() != null && b.getManifest().getId().equals(manifestId))
                .count() : 0;
            
            // Get total boxes for the order (including EXPECTED)
            List<Box> allOrderBoxes = boxRepository.findByOrderId(order.getId());
            int totalBoxes = allOrderBoxes.size();
            
            // Only include orders that have received boxes (not delivered)
            if (receivedCount > 0) {
                ManifestStopDto stop = new ManifestStopDto();
                stop.setOrderId(order.getOrderId());
                stop.setAddress(order.getCustomerAddress() != null ? order.getCustomerAddress() : "Unknown");
                
                // Determine box count and status
                if (manifestedCount > 0) {
                    // Some boxes are already assigned to this manifest
                    if (manifestedCount == receivedCount && receivedCount == totalBoxes) {
                        stop.setBoxes(totalBoxes);
                        stop.setBoxStatus("Complete");
                    } else if (manifestedCount == receivedCount) {
                        stop.setBoxes((int) receivedCount);
                        stop.setBoxStatus("Partial (" + receivedCount + " of " + totalBoxes + ")");
                    } else {
                        stop.setBoxes((int) manifestedCount + " of " + receivedCount);
                        stop.setBoxStatus("Partial (" + manifestedCount + " of " + receivedCount + " received)");
                    }
                } else {
                    // No boxes assigned to manifest yet
                    if (receivedCount == totalBoxes) {
                        stop.setBoxes(totalBoxes);
                        stop.setBoxStatus("Complete");
                    } else {
                        stop.setBoxes((int) receivedCount + " of " + totalBoxes);
                        stop.setBoxStatus("Partial (" + receivedCount + " of " + totalBoxes + ")");
                    }
                }
                
                stops.add(stop);
            }
        }
        
        return stops;
    }
}

package com.deliverysystem.service;

import com.deliverysystem.domain.Box;
import com.deliverysystem.domain.Driver;
import com.deliverysystem.domain.Manifest;
import com.deliverysystem.domain.Order;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.domain.Vehicle;
import com.deliverysystem.dto.CreateManifestRequest;
import com.deliverysystem.repository.BoxRepository;
import com.deliverysystem.repository.DriverRepository;
import com.deliverysystem.repository.ManifestRepository;
import com.deliverysystem.repository.OrderRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.VehicleRepository;
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
public class ManifestService {
    
    private static final Logger log = LoggerFactory.getLogger(ManifestService.class);
    private final ManifestRepository manifestRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final BoxRepository boxRepository;
    private final OrderRepository orderRepository;
    private final AuditService auditService;
    
    public ManifestService(ManifestRepository manifestRepository, RouteRepository routeRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository, BoxRepository boxRepository, OrderRepository orderRepository, AuditService auditService) {
        this.manifestRepository = manifestRepository;
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.boxRepository = boxRepository;
        this.orderRepository = orderRepository;
        this.auditService = auditService;
    }
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Transactional
    public Manifest createManifest(CreateManifestRequest request, User user) {
        Route route = routeRepository.findById(request.getRouteId())
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.getRouteId()));
        
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + request.getVehicleId()));
        
        Driver driver = driverRepository.findById(request.getDriverId())
            .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + request.getDriverId()));
        
        LocalDate date = LocalDate.parse(request.getDate(), DATE_FORMATTER);
        
        // Check if manifest already exists for this route and date
        Optional<Manifest> existing = manifestRepository.findByRouteIdAndDate(route.getId(), date);
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                String.format("Manifest already exists for route %s on date %s", route.getId(), date));
        }
        
        Manifest manifest = new Manifest();
        manifest.setRoute(route);
        manifest.setDate(date);
        manifest.setVehicle(vehicle);
        manifest.setDriver(driver);
        manifest.setStatus(Manifest.ManifestStatus.DRAFT);
        
        manifest = manifestRepository.save(manifest);
        
        // Assign boxes to manifest if provided
        if (request.getBoxIds() != null && !request.getBoxIds().isEmpty()) {
            List<Box> boxes = boxRepository.findAllById(request.getBoxIds());
            for (Box box : boxes) {
                box.setManifest(manifest);
                box.setStatus(Box.BoxStatus.MANIFESTED);
            }
            boxRepository.saveAll(boxes);
        }
        
        // Audit
        String depotId = route.getDepot() != null ? route.getDepot().getId() : null;
        auditService.logCreate(user, "Manifest", manifest.getId(), depotId, manifest);
        
        return manifest;
    }
    
    @Transactional
    public Manifest confirmManifest(String manifestId, User user) {
        Manifest manifest = manifestRepository.findById(manifestId)
            .orElseThrow(() -> new IllegalArgumentException("Manifest not found: " + manifestId));
        
        if (manifest.getStatus() != Manifest.ManifestStatus.DRAFT) {
            throw new IllegalStateException("Manifest is not in DRAFT status");
        }
        
        manifest.setStatus(Manifest.ManifestStatus.CONFIRMED);
        manifest = manifestRepository.save(manifest);
        
        // Audit
        String depotId = manifest.getRoute().getDepot() != null ? 
            manifest.getRoute().getDepot().getId() : null;
        auditService.logUpdate(user, "Manifest", manifest.getId(), depotId, 
            Manifest.ManifestStatus.DRAFT, Manifest.ManifestStatus.CONFIRMED);
        
        return manifest;
    }
    
    @Transactional
    public Manifest updateManifest(String manifestId, String driverId, String vehicleId, String date, User user) {
        Manifest manifest = manifestRepository.findById(manifestId)
            .orElseThrow(() -> new IllegalArgumentException("Manifest not found: " + manifestId));
        
        if (manifest.getStatus() != Manifest.ManifestStatus.DRAFT) {
            throw new IllegalStateException("Manifest is not in DRAFT status. Only DRAFT manifests can be updated.");
        }
        
        Manifest.ManifestStatus previousStatus = manifest.getStatus();
        Driver previousDriver = manifest.getDriver();
        Vehicle previousVehicle = manifest.getVehicle();
        LocalDate previousDate = manifest.getDate();
        
        // Update driver if provided
        if (driverId != null && !driverId.isEmpty()) {
            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));
            manifest.setDriver(driver);
        }
        
        // Update vehicle if provided
        if (vehicleId != null && !vehicleId.isEmpty()) {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));
            manifest.setVehicle(vehicle);
        }
        
        // Update date if provided
        if (date != null && !date.isEmpty()) {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            manifest.setDate(localDate);
        }
        
        manifest = manifestRepository.save(manifest);
        
        // Audit
        String depotId = manifest.getRoute().getDepot() != null ? 
            manifest.getRoute().getDepot().getId() : null;
        String beforeValue = String.format("Driver: %s, Vehicle: %s, Date: %s", 
            previousDriver.getId(), previousVehicle.getId(), previousDate);
        String afterValue = String.format("Driver: %s, Vehicle: %s, Date: %s", 
            manifest.getDriver().getId(), manifest.getVehicle().getId(), manifest.getDate());
        auditService.logUpdate(user, "Manifest", manifest.getId(), depotId, beforeValue, afterValue);
        
        log.info("Updated manifest {}", manifestId);
        
        return manifest;
    }
    
    @Transactional
    public Manifest removeStopFromManifest(String manifestId, String businessOrderId, User user) {
        Manifest manifest = manifestRepository.findById(manifestId)
            .orElseThrow(() -> new IllegalArgumentException("Manifest not found: " + manifestId));
        
        if (manifest.getStatus() != Manifest.ManifestStatus.DRAFT) {
            throw new IllegalStateException("Manifest is not in DRAFT status. Only DRAFT manifests can be modified.");
        }
        
        // Find orders on this route with the business orderId
        String routeId = manifest.getRoute().getId();
        List<Order> orders = orderRepository.findByRouteId(routeId).stream()
            .filter(order -> order.getOrderId().equals(businessOrderId))
            .toList();
        
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("Order " + businessOrderId + " not found on route for manifest " + manifestId);
        }
        
        // Find all boxes for these orders that are assigned to this manifest
        List<Box> boxesToRemove = new ArrayList<>();
        for (Order order : orders) {
            List<Box> boxes = boxRepository.findByOrderId(order.getId());
            List<Box> manifestBoxes = boxes.stream()
                .filter(box -> box.getManifest() != null && box.getManifest().getId().equals(manifestId))
                .toList();
            boxesToRemove.addAll(manifestBoxes);
        }
        
        if (boxesToRemove.isEmpty()) {
            throw new IllegalArgumentException("Order " + businessOrderId + " has no boxes assigned to manifest " + manifestId);
        }
        
        // Unassign boxes from manifest
        for (Box box : boxesToRemove) {
            box.setManifest(null);
            // Reset status to RECEIVED if it was MANIFESTED
            if (box.getStatus() == Box.BoxStatus.MANIFESTED) {
                box.setStatus(Box.BoxStatus.RECEIVED);
            }
        }
        boxRepository.saveAll(boxesToRemove);
        
        // Audit
        String depotId = manifest.getRoute().getDepot() != null ? 
            manifest.getRoute().getDepot().getId() : null;
        auditService.logUpdate(user, "Manifest", manifest.getId(), depotId, 
            "Order " + businessOrderId + " included", "Order " + businessOrderId + " removed");
        
        log.info("Removed order {} from manifest {}", businessOrderId, manifestId);
        
        return manifest;
    }
}

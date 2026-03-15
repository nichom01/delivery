package com.deliverysystem.service;

import com.deliverysystem.domain.Box;
import com.deliverysystem.domain.Driver;
import com.deliverysystem.domain.Manifest;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.domain.Vehicle;
import com.deliverysystem.dto.CreateManifestRequest;
import com.deliverysystem.repository.BoxRepository;
import com.deliverysystem.repository.DriverRepository;
import com.deliverysystem.repository.ManifestRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final AuditService auditService;
    
    public ManifestService(ManifestRepository manifestRepository, RouteRepository routeRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository, BoxRepository boxRepository, AuditService auditService) {
        this.manifestRepository = manifestRepository;
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.boxRepository = boxRepository;
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
}

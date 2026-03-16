package com.deliverysystem.service;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.User;
import com.deliverysystem.domain.Vehicle;
import com.deliverysystem.repository.DepotRepository;
import com.deliverysystem.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final VehicleRepository vehicleRepository;
    private final DepotRepository depotRepository;
    private final AuditService auditService;

    public VehicleService(VehicleRepository vehicleRepository, DepotRepository depotRepository, AuditService auditService) {
        this.vehicleRepository = vehicleRepository;
        this.depotRepository = depotRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Vehicle createVehicle(String registration, String make, String model, String capacity, 
                                  String motDate, String nextServiceDue, String depotId, User user) {
        // Check if registration already exists
        Vehicle existing = vehicleRepository.findByRegistration(registration);
        if (existing != null) {
            throw new IllegalArgumentException("Vehicle with registration '" + registration + "' already exists");
        }

        Depot depot = depotRepository.findById(depotId)
            .orElseThrow(() -> new IllegalArgumentException("Depot not found: " + depotId));

        Vehicle vehicle = new Vehicle();
        vehicle.setRegistration(registration);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setCapacity(capacity);
        vehicle.setDepot(depot);
        vehicle.setStatus("ACTIVE");

        if (motDate != null && !motDate.isEmpty()) {
            try {
                vehicle.setMotDate(LocalDate.parse(motDate, DATE_FORMATTER));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid MOT date format");
            }
        }

        if (nextServiceDue != null && !nextServiceDue.isEmpty()) {
            try {
                vehicle.setNextServiceDue(LocalDate.parse(nextServiceDue, DATE_FORMATTER));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid next service date format");
            }
        }

        vehicle = vehicleRepository.save(vehicle);

        // Audit
        String vehicleInfo = String.format("Vehicle{id='%s', registration='%s', make='%s', model='%s'}",
            vehicle.getId(), vehicle.getRegistration(), vehicle.getMake(), vehicle.getModel());
        auditService.logCreate(user, "Vehicle", vehicle.getId(), depotId, vehicleInfo);

        log.info("Created vehicle {} ({})", vehicle.getId(), vehicle.getRegistration());

        return vehicle;
    }

    @Transactional
    public Vehicle updateVehicle(String vehicleId, String registration, String make, String model, 
                                 String capacity, String motDate, String nextServiceDue, String status, User user) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        String beforeValue = String.format("Registration: %s, Make: %s, Model: %s, Capacity: %s",
            vehicle.getRegistration(), vehicle.getMake(), vehicle.getModel(), vehicle.getCapacity());

        // Check if registration is being changed and if new registration already exists
        if (registration != null && !registration.isEmpty() && !registration.equals(vehicle.getRegistration())) {
            Vehicle existing = vehicleRepository.findByRegistration(registration);
            if (existing != null && !existing.getId().equals(vehicleId)) {
                throw new IllegalArgumentException("Vehicle with registration '" + registration + "' already exists");
            }
            vehicle.setRegistration(registration);
        }

        if (make != null && !make.isEmpty()) {
            vehicle.setMake(make);
        }
        if (model != null && !model.isEmpty()) {
            vehicle.setModel(model);
        }
        if (capacity != null && !capacity.isEmpty()) {
            vehicle.setCapacity(capacity);
        }
        if (status != null && !status.isEmpty()) {
            vehicle.setStatus(status);
        }

        if (motDate != null && !motDate.isEmpty()) {
            try {
                vehicle.setMotDate(LocalDate.parse(motDate, DATE_FORMATTER));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid MOT date format");
            }
        }

        if (nextServiceDue != null && !nextServiceDue.isEmpty()) {
            try {
                vehicle.setNextServiceDue(LocalDate.parse(nextServiceDue, DATE_FORMATTER));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid next service date format");
            }
        }

        vehicle = vehicleRepository.save(vehicle);

        // Audit
        String depotId = vehicle.getDepot() != null ? vehicle.getDepot().getId() : null;
        String afterValue = String.format("Registration: %s, Make: %s, Model: %s, Capacity: %s",
            vehicle.getRegistration(), vehicle.getMake(), vehicle.getModel(), vehicle.getCapacity());
        auditService.logUpdate(user, "Vehicle", vehicle.getId(), depotId, beforeValue, afterValue);

        log.info("Updated vehicle {} ({})", vehicle.getId(), vehicle.getRegistration());

        return vehicle;
    }
}

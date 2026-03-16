package com.deliverysystem.service;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.User;
import com.deliverysystem.repository.DepotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DepotService {
    
    private static final Logger log = LoggerFactory.getLogger(DepotService.class);
    private final DepotRepository depotRepository;
    private final AuditService auditService;
    
    public DepotService(DepotRepository depotRepository, AuditService auditService) {
        this.depotRepository = depotRepository;
        this.auditService = auditService;
    }
    
    @Transactional
    public Depot createDepot(String name, String address, String latitude, String longitude, User user) {
        // Check if depot name already exists
        if (depotRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Depot with name '" + name + "' already exists");
        }
        
        Depot depot = new Depot();
        depot.setName(name);
        depot.setAddress(address);
        
        if (latitude != null && !latitude.isEmpty()) {
            try {
                depot.setLatitude(new BigDecimal(latitude));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid latitude format");
            }
        }
        
        if (longitude != null && !longitude.isEmpty()) {
            try {
                depot.setLongitude(new BigDecimal(longitude));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid longitude format");
            }
        }
        
        depot = depotRepository.save(depot);
        
        // Audit - use string representation to avoid lazy loading issues
        String depotInfo = String.format("Depot{id='%s', name='%s', address='%s'}", 
            depot.getId(), depot.getName(), depot.getAddress());
        auditService.logCreate(user, "Depot", depot.getId(), depot.getId(), depotInfo);
        
        log.info("Created depot {} ({})", depot.getId(), depot.getName());
        
        return depot;
    }
    
    @Transactional
    public Depot updateDepot(String depotId, String name, String address, String latitude, String longitude, User user) {
        Depot depot = depotRepository.findById(depotId)
            .orElseThrow(() -> new IllegalArgumentException("Depot not found: " + depotId));
        
        String beforeValue = String.format("Name: %s, Address: %s", depot.getName(), depot.getAddress());
        
        if (name != null && !name.isEmpty()) {
            // Check if new name conflicts with existing depot
            depotRepository.findByName(name)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(depotId)) {
                        throw new IllegalArgumentException("Depot with name '" + name + "' already exists");
                    }
                });
            depot.setName(name);
        }
        
        if (address != null && !address.isEmpty()) {
            depot.setAddress(address);
        }
        
        if (latitude != null && !latitude.isEmpty()) {
            try {
                depot.setLatitude(new BigDecimal(latitude));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid latitude format");
            }
        }
        
        if (longitude != null && !longitude.isEmpty()) {
            try {
                depot.setLongitude(new BigDecimal(longitude));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid longitude format");
            }
        }
        
        depot = depotRepository.save(depot);
        
        // Audit
        String afterValue = String.format("Name: %s, Address: %s", depot.getName(), depot.getAddress());
        auditService.logUpdate(user, "Depot", depot.getId(), depot.getId(), beforeValue, afterValue);
        
        log.info("Updated depot {} ({})", depot.getId(), depot.getName());
        
        return depot;
    }
}

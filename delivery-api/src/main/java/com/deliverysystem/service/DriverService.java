package com.deliverysystem.service;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.Driver;
import com.deliverysystem.domain.User;
import com.deliverysystem.repository.DepotRepository;
import com.deliverysystem.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final DriverRepository driverRepository;
    private final DepotRepository depotRepository;
    private final AuditService auditService;

    public DriverService(DriverRepository driverRepository, DepotRepository depotRepository, AuditService auditService) {
        this.driverRepository = driverRepository;
        this.depotRepository = depotRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Driver createDriver(String name, String contact, String licenceNumber, String licenceExpiry,
                               String shiftInfo, String depotId, User user) {
        Depot depot = depotRepository.findById(depotId)
            .orElseThrow(() -> new IllegalArgumentException("Depot not found: " + depotId));

        Driver driver = new Driver();
        driver.setName(name);
        driver.setContact(contact);
        driver.setLicenceNumber(licenceNumber);
        driver.setShiftInfo(shiftInfo);
        driver.setDepot(depot);
        driver.setStatus("ACTIVE");

        if (licenceExpiry != null && !licenceExpiry.isEmpty()) {
            try {
                driver.setLicenceExpiry(LocalDate.parse(licenceExpiry, DATE_FORMATTER));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid licence expiry date format");
            }
        }

        driver = driverRepository.save(driver);

        // Audit
        String driverInfo = String.format("Driver{id='%s', name='%s', licenceNumber='%s'}",
            driver.getId(), driver.getName(), driver.getLicenceNumber());
        auditService.logCreate(user, "Driver", driver.getId(), depotId, driverInfo);

        log.info("Created driver {} ({})", driver.getId(), driver.getName());

        return driver;
    }

    @Transactional
    public Driver updateDriver(String driverId, String name, String contact, String licenceNumber,
                               String licenceExpiry, String shiftInfo, String status, User user) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        String beforeValue = String.format("Name: %s, Contact: %s, LicenceNumber: %s",
            driver.getName(), driver.getContact(), driver.getLicenceNumber());

        if (name != null && !name.isEmpty()) {
            driver.setName(name);
        }
        if (contact != null) {
            driver.setContact(contact);
        }
        if (licenceNumber != null) {
            driver.setLicenceNumber(licenceNumber);
        }
        if (shiftInfo != null) {
            driver.setShiftInfo(shiftInfo);
        }
        if (status != null && !status.isEmpty()) {
            driver.setStatus(status);
        }

        if (licenceExpiry != null && !licenceExpiry.isEmpty()) {
            try {
                driver.setLicenceExpiry(LocalDate.parse(licenceExpiry, DATE_FORMATTER));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid licence expiry date format");
            }
        }

        driver = driverRepository.save(driver);

        // Audit
        String depotId = driver.getDepot() != null ? driver.getDepot().getId() : null;
        String afterValue = String.format("Name: %s, Contact: %s, LicenceNumber: %s",
            driver.getName(), driver.getContact(), driver.getLicenceNumber());
        auditService.logUpdate(user, "Driver", driver.getId(), depotId, beforeValue, afterValue);

        log.info("Updated driver {} ({})", driver.getId(), driver.getName());

        return driver;
    }
}

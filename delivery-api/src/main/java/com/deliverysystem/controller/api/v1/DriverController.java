package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Driver;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.DriverDto;
import com.deliverysystem.repository.DriverRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {
    
    private final DriverRepository driverRepository;
    
    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverDto>>> getDrivers(
            @RequestParam(required = false) String depotId) {
        List<Driver> drivers;
        if (depotId != null) {
            drivers = driverRepository.findByDepotId(depotId);
        } else {
            drivers = driverRepository.findAll();
        }
        
        List<DriverDto> dtos = drivers.stream().map(driver -> {
            DriverDto dto = new DriverDto();
            dto.setId(driver.getId());
            dto.setDepotId(driver.getDepot().getId());
            dto.setName(driver.getName());
            dto.setLicenceNo(driver.getLicenceNumber());
            if (driver.getLicenceExpiry() != null) {
                dto.setExpiry(driver.getLicenceExpiry().format(DATE_FORMATTER));
            }
            dto.setContact(driver.getContact());
            dto.setStatus(driver.getStatus());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}

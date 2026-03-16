package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Driver;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreateDriverRequest;
import com.deliverysystem.dto.UpdateDriverRequest;
import com.deliverysystem.dto.DriverDto;
import com.deliverysystem.repository.DriverRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {
    
    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    
    public DriverController(DriverRepository driverRepository, DriverService driverService,
                            UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.driverRepository = driverRepository;
        this.driverService = driverService;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
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
    
    @PostMapping
    public ResponseEntity<ApiResponse<DriverDto>> createDriver(
            @Valid @RequestBody CreateDriverRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Driver driver = driverService.createDriver(
            request.getName(),
            request.getContact(),
            request.getLicenceNumber(),
            request.getLicenceExpiry(),
            request.getShiftInfo(),
            request.getDepotId(),
            user
        );
        
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
        
        return ResponseEntity.ok(ApiResponse.success("Driver created successfully", dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverDto>> updateDriver(
            @PathVariable String id,
            @Valid @RequestBody UpdateDriverRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Driver driver = driverService.updateDriver(
            id,
            request.getName(),
            request.getContact(),
            request.getLicenceNumber(),
            request.getLicenceExpiry(),
            request.getShiftInfo(),
            request.getStatus(),
            user
        );
        
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
        
        return ResponseEntity.ok(ApiResponse.success("Driver updated successfully", dto));
    }
}

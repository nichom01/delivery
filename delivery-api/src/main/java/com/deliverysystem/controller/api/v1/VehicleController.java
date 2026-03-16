package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.User;
import com.deliverysystem.domain.Vehicle;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreateVehicleRequest;
import com.deliverysystem.dto.UpdateVehicleRequest;
import com.deliverysystem.dto.VehicleDto;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.repository.VehicleRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
    
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    
    public VehicleController(VehicleRepository vehicleRepository, VehicleService vehicleService,
                             UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleService = vehicleService;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getVehicles(
            @RequestParam(required = false) String depotId) {
        List<Vehicle> vehicles;
        if (depotId != null) {
            vehicles = vehicleRepository.findByDepotId(depotId);
        } else {
            vehicles = vehicleRepository.findAll();
        }
        
        List<VehicleDto> dtos = vehicles.stream().map(vehicle -> {
            VehicleDto dto = new VehicleDto();
            dto.setId(vehicle.getId());
            dto.setDepotId(vehicle.getDepot().getId());
            dto.setRegistration(vehicle.getRegistration());
            dto.setMakeModel(vehicle.getMake() + " " + vehicle.getModel());
            dto.setCapacity(vehicle.getCapacity());
            if (vehicle.getMotDate() != null) {
                dto.setMotDue(vehicle.getMotDate().format(DATE_FORMATTER));
            }
            if (vehicle.getNextServiceDue() != null) {
                dto.setNextService(vehicle.getNextServiceDue().format(DATE_FORMATTER));
            }
            dto.setStatus(vehicle.getStatus());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleDto>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Vehicle vehicle = vehicleService.createVehicle(
            request.getRegistration(),
            request.getMake(),
            request.getModel(),
            request.getCapacity(),
            request.getMotDate(),
            request.getNextServiceDue(),
            request.getDepotId(),
            user
        );
        
        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        dto.setDepotId(vehicle.getDepot().getId());
        dto.setRegistration(vehicle.getRegistration());
        String makeModel = ((vehicle.getMake() != null ? vehicle.getMake() : "") + " " + 
                        (vehicle.getModel() != null ? vehicle.getModel() : "")).trim();
        dto.setMakeModel(makeModel);
        dto.setCapacity(vehicle.getCapacity());
        if (vehicle.getMotDate() != null) {
            dto.setMotDue(vehicle.getMotDate().format(DATE_FORMATTER));
        }
        if (vehicle.getNextServiceDue() != null) {
            dto.setNextService(vehicle.getNextServiceDue().format(DATE_FORMATTER));
        }
        dto.setStatus(vehicle.getStatus());
        
        return ResponseEntity.ok(ApiResponse.success("Vehicle created successfully", dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleDto>> updateVehicle(
            @PathVariable String id,
            @Valid @RequestBody UpdateVehicleRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Vehicle vehicle = vehicleService.updateVehicle(
            id,
            request.getRegistration(),
            request.getMake(),
            request.getModel(),
            request.getCapacity(),
            request.getMotDate(),
            request.getNextServiceDue(),
            request.getStatus(),
            user
        );
        
        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        dto.setDepotId(vehicle.getDepot().getId());
        dto.setRegistration(vehicle.getRegistration());
        String makeModel = ((vehicle.getMake() != null ? vehicle.getMake() : "") + " " + 
                        (vehicle.getModel() != null ? vehicle.getModel() : "")).trim();
        dto.setMakeModel(makeModel);
        dto.setCapacity(vehicle.getCapacity());
        if (vehicle.getMotDate() != null) {
            dto.setMotDue(vehicle.getMotDate().format(DATE_FORMATTER));
        }
        if (vehicle.getNextServiceDue() != null) {
            dto.setNextService(vehicle.getNextServiceDue().format(DATE_FORMATTER));
        }
        dto.setStatus(vehicle.getStatus());
        
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", dto));
    }
}

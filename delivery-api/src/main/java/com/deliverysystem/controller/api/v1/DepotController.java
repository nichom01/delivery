package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreateDepotRequest;
import com.deliverysystem.dto.DepotDto;
import com.deliverysystem.dto.UpdateDepotRequest;
import com.deliverysystem.repository.DepotRepository;
import com.deliverysystem.repository.DriverRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.repository.VehicleRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.AuditService;
import com.deliverysystem.service.DepotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/depots")
public class DepotController {
    
    private final DepotRepository depotRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;
    private final DepotService depotService;
    
    public DepotController(DepotRepository depotRepository, RouteRepository routeRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository, UserRepository userRepository, JwtTokenProvider tokenProvider, AuditService auditService, DepotService depotService) {
        this.depotRepository = depotRepository;
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
        this.depotService = depotService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepotDto>>> getAllDepots() {
        List<Depot> depots = depotRepository.findAll();
        
        List<DepotDto> depotDtos = depots.stream().map(depot -> {
            DepotDto dto = new DepotDto();
            dto.setId(depot.getId());
            dto.setName(depot.getName());
            dto.setLocation(depot.getAddress());
            dto.setRoutesCount(routeRepository.findByDepotId(depot.getId()).size());
            dto.setVehiclesCount(vehicleRepository.findByDepotId(depot.getId()).size());
            dto.setDriversCount(driverRepository.findByDepotId(depot.getId()).size());
            dto.setStatus("ACTIVE");
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(depotDtos));
    }
    
    @GetMapping("/{id}/routes")
    public ResponseEntity<ApiResponse<List<com.deliverysystem.dto.RouteDto>>> getRoutesByDepot(
            @PathVariable String id) {
        List<com.deliverysystem.domain.Route> routes = routeRepository.findByDepotId(id);
        
        List<com.deliverysystem.dto.RouteDto> routeDtos = routes.stream().map(route -> {
            com.deliverysystem.dto.RouteDto dto = new com.deliverysystem.dto.RouteDto();
            dto.setId(route.getId());
            dto.setDepotId(route.getDepot().getId());
            dto.setCode(route.getCode());
            dto.setName(route.getName());
            dto.setCoverage("");
            dto.setPostcodeRulesCount(route.getPostcodeRules().size());
            dto.setStatus("ACTIVE");
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(routeDtos));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<DepotDto>> createDepot(
            @Valid @RequestBody CreateDepotRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Depot depot = depotService.createDepot(
            request.getName(),
            request.getAddress(),
            request.getLatitude(),
            request.getLongitude(),
            user
        );
        
        DepotDto dto = new DepotDto();
        dto.setId(depot.getId());
        dto.setName(depot.getName());
        dto.setLocation(depot.getAddress());
        dto.setRoutesCount(routeRepository.findByDepotId(depot.getId()).size());
        dto.setVehiclesCount(vehicleRepository.findByDepotId(depot.getId()).size());
        dto.setDriversCount(driverRepository.findByDepotId(depot.getId()).size());
        dto.setStatus("ACTIVE");
        
        return ResponseEntity.ok(ApiResponse.success("Depot created successfully", dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepotDto>> updateDepot(
            @PathVariable String id,
            @Valid @RequestBody UpdateDepotRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Depot depot = depotService.updateDepot(
            id,
            request.getName(),
            request.getAddress(),
            request.getLatitude(),
            request.getLongitude(),
            user
        );
        
        DepotDto dto = new DepotDto();
        dto.setId(depot.getId());
        dto.setName(depot.getName());
        dto.setLocation(depot.getAddress());
        dto.setRoutesCount(routeRepository.findByDepotId(depot.getId()).size());
        dto.setVehiclesCount(vehicleRepository.findByDepotId(depot.getId()).size());
        dto.setDriversCount(driverRepository.findByDepotId(depot.getId()).size());
        dto.setStatus("ACTIVE");
        
        return ResponseEntity.ok(ApiResponse.success("Depot updated successfully", dto));
    }
}

package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreateRouteRequest;
import com.deliverysystem.dto.RouteDto;
import com.deliverysystem.dto.RouteDrilldownDto;
import com.deliverysystem.dto.UpdateRouteRequest;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final RouteRepository routeRepository;
    private final RouteService routeService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    
    public RouteController(RouteRepository routeRepository, RouteService routeService, UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.routeRepository = routeRepository;
        this.routeService = routeService;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteDto>> getRoute(@PathVariable String id) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        
        RouteDto dto = new RouteDto();
        dto.setId(route.getId());
        dto.setDepotId(route.getDepot().getId());
        dto.setCode(route.getCode());
        dto.setName(route.getName());
        dto.setCoverage(route.getDescription() != null ? route.getDescription() : "");
        dto.setPostcodeRulesCount(route.getPostcodeRules().size());
        dto.setStatus("ACTIVE");
        
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
    
    @GetMapping("/{id}/drilldown")
    public ResponseEntity<ApiResponse<RouteDrilldownDto>> getRouteDrilldown(
            @PathVariable String id,
            @RequestParam(required = false) String date) {
        
        LocalDate localDate = date != null ? LocalDate.parse(date, DATE_FORMATTER) : LocalDate.now();
        RouteDrilldownDto drilldown = routeService.getRouteDrilldown(id, localDate);
        
        return ResponseEntity.ok(ApiResponse.success(drilldown));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<RouteDto>> createRoute(
            @Valid @RequestBody CreateRouteRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Route route = routeService.createRoute(
            request.getCode(), request.getName(), request.getDescription(), request.getDepotId(), user);
        
        RouteDto dto = new RouteDto();
        dto.setId(route.getId());
        dto.setDepotId(route.getDepot().getId());
        dto.setCode(route.getCode());
        dto.setName(route.getName());
        dto.setCoverage(route.getDescription() != null ? route.getDescription() : "");
        dto.setPostcodeRulesCount(route.getPostcodeRules().size());
        dto.setStatus("ACTIVE");
        
        return ResponseEntity.ok(ApiResponse.success("Route created successfully", dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteDto>> updateRoute(
            @PathVariable String id,
            @Valid @RequestBody UpdateRouteRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Route route = routeService.updateRoute(id, request.getName(), request.getDescription(), user);
        
        RouteDto dto = new RouteDto();
        dto.setId(route.getId());
        dto.setDepotId(route.getDepot().getId());
        dto.setCode(route.getCode());
        dto.setName(route.getName());
        dto.setCoverage(route.getDescription() != null ? route.getDescription() : "");
        dto.setPostcodeRulesCount(route.getPostcodeRules().size());
        dto.setStatus("ACTIVE");
        
        return ResponseEntity.ok(ApiResponse.success("Route updated successfully", dto));
    }
}

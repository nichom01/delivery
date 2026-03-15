package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Route;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.RouteDto;
import com.deliverysystem.repository.RouteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {
    
    private final RouteRepository routeRepository;
    
    public RouteController(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
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
        dto.setCoverage("");
        dto.setPostcodeRulesCount(route.getPostcodeRules().size());
        dto.setStatus("ACTIVE");
        
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}

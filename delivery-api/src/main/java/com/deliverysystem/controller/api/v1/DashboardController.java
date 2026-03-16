package com.deliverysystem.controller.api.v1;

import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.DashboardDto;
import com.deliverysystem.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DashboardService dashboardService;
    
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard(
            @RequestParam(required = false) String depotId,
            @RequestParam(required = false) String date) {
        
        // Default to today if not provided
        LocalDate localDate = date != null ? LocalDate.parse(date, DATE_FORMATTER) : LocalDate.now();
        
        DashboardDto dashboard = dashboardService.getDashboard(depotId, localDate);
        
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}

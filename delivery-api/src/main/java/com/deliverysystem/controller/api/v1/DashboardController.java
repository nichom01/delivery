package com.deliverysystem.controller.api.v1;

import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.DashboardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public DashboardController() {
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard(
            @RequestParam(required = false) String depotId,
            @RequestParam(required = false) String date) {
        
        // Default to today if not provided
        LocalDate localDate = date != null ? LocalDate.parse(date, DATE_FORMATTER) : LocalDate.now();
        
        // TODO: Implement dashboard logic
        DashboardDto dashboard = new DashboardDto();
        dashboard.setDate(localDate.format(DATE_FORMATTER));
        // Dashboard summary and route summaries would be populated here
        
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}

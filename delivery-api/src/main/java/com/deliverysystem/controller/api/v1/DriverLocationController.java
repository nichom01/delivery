package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.DriverLocationSubmitResponse;
import com.deliverysystem.dto.SubmitDriverLocationsRequest;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.service.DriverLocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/driver-locations")
public class DriverLocationController {

    private final UserRepository userRepository;
    private final DriverLocationService driverLocationService;

    public DriverLocationController(UserRepository userRepository, DriverLocationService driverLocationService) {
        this.userRepository = userRepository;
        this.driverLocationService = driverLocationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DriverLocationSubmitResponse>> submitLocations(
            @Valid @RequestBody SubmitDriverLocationsRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int saved = driverLocationService.saveSamples(user, request.getLocations());
        return ResponseEntity.ok(ApiResponse.success(
            "Locations saved successfully",
            new DriverLocationSubmitResponse(saved)));
    }
}

package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Manifest;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreateManifestRequest;
import com.deliverysystem.dto.ManifestDto;
import com.deliverysystem.repository.ManifestRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.ManifestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/manifests")
public class ManifestController {
    
    private final ManifestService manifestService;
    private final ManifestRepository manifestRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    
    public ManifestController(ManifestService manifestService, ManifestRepository manifestRepository, UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.manifestService = manifestService;
        this.manifestRepository = manifestRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ManifestDto>>> getManifests(
            @RequestParam(required = false) String depotId,
            @RequestParam(required = false) String date) {
        List<Manifest> manifests;
        if (depotId != null && date != null) {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            manifests = manifestRepository.findByRouteDepotIdAndDate(depotId, localDate);
        } else if (depotId != null) {
            manifests = manifestRepository.findByRouteDepotId(depotId);
        } else {
            manifests = manifestRepository.findAll();
        }
        
        List<ManifestDto> dtos = manifests.stream().map(this::toDto).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ManifestDto>> createManifest(
            @Valid @RequestBody CreateManifestRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Manifest manifest = manifestService.createManifest(request, user);
        return ResponseEntity.ok(ApiResponse.success("Manifest created", toDto(manifest)));
    }
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ManifestDto>> confirmManifest(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Manifest manifest = manifestService.confirmManifest(id, user);
        return ResponseEntity.ok(ApiResponse.success("Manifest confirmed", toDto(manifest)));
    }
    
    private ManifestDto toDto(Manifest manifest) {
        ManifestDto dto = new ManifestDto();
        dto.setId(manifest.getId());
        dto.setRouteId(manifest.getRoute().getId());
        dto.setDate(manifest.getDate().format(DATE_FORMATTER));
        dto.setDriverId(manifest.getDriver().getId());
        dto.setVehicleId(manifest.getVehicle().getId());
        dto.setStatus(manifest.getStatus().name());
        // Stops would be populated from boxes/orders
        return dto;
    }
}

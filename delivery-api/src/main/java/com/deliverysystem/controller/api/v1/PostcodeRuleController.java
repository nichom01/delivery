package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.PostcodeRule;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreatePostcodeRuleRequest;
import com.deliverysystem.dto.PostcodeHierarchyLevelDto;
import com.deliverysystem.dto.PostcodeLookupDto;
import com.deliverysystem.dto.PostcodeRuleDto;
import com.deliverysystem.dto.UpdatePostcodeRuleRequest;
import com.deliverysystem.repository.PostcodeRuleRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.AuditService;
import com.deliverysystem.service.PostcodeRoutingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/postcode-rules")
public class PostcodeRuleController {
    
    private final PostcodeRuleRepository postcodeRuleRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;
    private final PostcodeRoutingService postcodeRoutingService;
    
    public PostcodeRuleController(PostcodeRuleRepository postcodeRuleRepository, RouteRepository routeRepository, UserRepository userRepository, JwtTokenProvider tokenProvider, AuditService auditService, PostcodeRoutingService postcodeRoutingService) {
        this.postcodeRuleRepository = postcodeRuleRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
        this.postcodeRoutingService = postcodeRoutingService;
    }
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostcodeRuleDto>>> getPostcodeRules(
            @RequestParam(required = false) String depotId) {
        List<PostcodeRule> rules;
        if (depotId != null) {
            rules = postcodeRuleRepository.findByRouteDepotId(depotId);
        } else {
            rules = postcodeRuleRepository.findAll();
        }
        
        List<PostcodeRuleDto> dtos = rules.stream().map(rule -> {
            PostcodeRuleDto dto = new PostcodeRuleDto();
            dto.setId(rule.getId());
            dto.setPattern(rule.getPattern());
            dto.setLevel(rule.getLevel().name().toLowerCase());
            dto.setRouteId(rule.getRoute().getId());
            dto.setRouteName(rule.getRoute().getName());
            dto.setEffectiveFrom(rule.getEffectiveFrom().format(DATE_FORMATTER));
            if (rule.getEffectiveTo() != null) {
                dto.setEffectiveTo(rule.getEffectiveTo().format(DATE_FORMATTER));
            }
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<PostcodeRuleDto>> createPostcodeRule(
            @Valid @RequestBody CreatePostcodeRuleRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Route route = routeRepository.findById(request.getRouteId())
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.getRouteId()));
        
        PostcodeRule rule = new PostcodeRule();
        rule.setPattern(request.getPattern());
        rule.setLevel(PostcodeRule.PostcodeLevel.valueOf(request.getLevel().toUpperCase()));
        rule.setRoute(route);
        rule.setEffectiveFrom(LocalDate.parse(request.getEffectiveFrom(), DATE_FORMATTER));
        if (request.getEffectiveTo() != null) {
            rule.setEffectiveTo(LocalDate.parse(request.getEffectiveTo(), DATE_FORMATTER));
        }
        
        rule = postcodeRuleRepository.save(rule);
        
        // Audit
        String depotId = route.getDepot() != null ? route.getDepot().getId() : null;
        auditService.logCreate(user, "PostcodeRule", rule.getId(), depotId, rule);
        
        PostcodeRuleDto dto = new PostcodeRuleDto();
        dto.setId(rule.getId());
        dto.setPattern(rule.getPattern());
        dto.setLevel(rule.getLevel().name().toLowerCase());
        dto.setRouteId(rule.getRoute().getId());
        dto.setRouteName(rule.getRoute().getName());
        dto.setEffectiveFrom(rule.getEffectiveFrom().format(DATE_FORMATTER));
        if (rule.getEffectiveTo() != null) {
            dto.setEffectiveTo(rule.getEffectiveTo().format(DATE_FORMATTER));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Postcode rule created", dto));
    }
    
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<PostcodeLookupDto>> lookupPostcode(
            @RequestParam String postcode) {
        
        List<PostcodeRule> hierarchy = postcodeRoutingService.getPostcodeHierarchy(postcode, LocalDate.now());
        
        List<PostcodeHierarchyLevelDto> hierarchyDtos = hierarchy.stream()
            .map(rule -> {
                PostcodeHierarchyLevelDto dto = new PostcodeHierarchyLevelDto();
                dto.setLevel(rule.getLevel().name().toLowerCase());
                dto.setPattern(rule.getPattern());
                dto.setRouteName(rule.getRoute().getName());
                // Mark the most specific match as the match
                dto.setIsMatch(hierarchy.indexOf(rule) == 0);
                return dto;
            })
            .collect(Collectors.toList());
        
        PostcodeLookupDto lookupDto = new PostcodeLookupDto();
        lookupDto.setHierarchy(hierarchyDtos);
        
        return ResponseEntity.ok(ApiResponse.success(lookupDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostcodeRuleDto>> updatePostcodeRule(
            @PathVariable String id,
            @Valid @RequestBody UpdatePostcodeRuleRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        PostcodeRule rule = postcodeRuleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Postcode rule not found: " + id));
        
        String previousPattern = rule.getPattern();
        PostcodeRule.PostcodeLevel previousLevel = rule.getLevel();
        Route previousRoute = rule.getRoute();
        LocalDate previousEffectiveFrom = rule.getEffectiveFrom();
        LocalDate previousEffectiveTo = rule.getEffectiveTo();
        
        // Update pattern if provided
        if (request.getPattern() != null && !request.getPattern().isEmpty()) {
            rule.setPattern(request.getPattern().trim().toUpperCase());
        }
        
        // Update level if provided
        if (request.getLevel() != null && !request.getLevel().isEmpty()) {
            rule.setLevel(PostcodeRule.PostcodeLevel.valueOf(request.getLevel().toUpperCase()));
        }
        
        // Update route if provided
        if (request.getRouteId() != null && !request.getRouteId().isEmpty()) {
            Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + request.getRouteId()));
            rule.setRoute(route);
        }
        
        // Update effective dates if provided
        if (request.getEffectiveFrom() != null && !request.getEffectiveFrom().isEmpty()) {
            rule.setEffectiveFrom(LocalDate.parse(request.getEffectiveFrom(), DATE_FORMATTER));
        }
        if (request.getEffectiveTo() != null && !request.getEffectiveTo().isEmpty()) {
            rule.setEffectiveTo(LocalDate.parse(request.getEffectiveTo(), DATE_FORMATTER));
        } else if (request.getEffectiveTo() != null) {
            // Empty string means clear the effectiveTo
            rule.setEffectiveTo(null);
        }
        
        // Validate effective date range
        if (rule.getEffectiveTo() != null && rule.getEffectiveTo().isBefore(rule.getEffectiveFrom())) {
            throw new IllegalArgumentException("Effective 'to' date cannot be before 'from' date");
        }
        
        rule = postcodeRuleRepository.save(rule);
        
        // Audit
        String depotId = rule.getRoute().getDepot() != null ? rule.getRoute().getDepot().getId() : null;
        String beforeValue = String.format("Pattern: %s, Level: %s, Route: %s, From: %s, To: %s",
            previousPattern, previousLevel, previousRoute.getId(), previousEffectiveFrom, previousEffectiveTo);
        String afterValue = String.format("Pattern: %s, Level: %s, Route: %s, From: %s, To: %s",
            rule.getPattern(), rule.getLevel(), rule.getRoute().getId(), rule.getEffectiveFrom(), rule.getEffectiveTo());
        auditService.logUpdate(user, "PostcodeRule", rule.getId(), depotId, beforeValue, afterValue);
        
        PostcodeRuleDto dto = new PostcodeRuleDto();
        dto.setId(rule.getId());
        dto.setPattern(rule.getPattern());
        dto.setLevel(rule.getLevel().name().toLowerCase());
        dto.setRouteId(rule.getRoute().getId());
        dto.setRouteName(rule.getRoute().getName());
        dto.setEffectiveFrom(rule.getEffectiveFrom().format(DATE_FORMATTER));
        if (rule.getEffectiveTo() != null) {
            dto.setEffectiveTo(rule.getEffectiveTo().format(DATE_FORMATTER));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Postcode rule updated", dto));
    }
}

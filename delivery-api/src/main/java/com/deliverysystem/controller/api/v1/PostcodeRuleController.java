package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.PostcodeRule;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreatePostcodeRuleRequest;
import com.deliverysystem.dto.PostcodeRuleDto;
import com.deliverysystem.repository.PostcodeRuleRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.AuditService;
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
    
    public PostcodeRuleController(PostcodeRuleRepository postcodeRuleRepository, RouteRepository routeRepository, UserRepository userRepository, JwtTokenProvider tokenProvider, AuditService auditService) {
        this.postcodeRuleRepository = postcodeRuleRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
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
}

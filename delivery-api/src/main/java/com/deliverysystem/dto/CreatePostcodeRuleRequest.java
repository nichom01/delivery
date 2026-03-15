package com.deliverysystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePostcodeRuleRequest {
    @NotBlank
    private String pattern;
    
    @NotBlank
    private String level; // 'full' | 'sector' | 'district' | 'area' | 'letter'
    
    @NotBlank
    private String routeId;
    
    @NotNull
    private String effectiveFrom;
    
    private String effectiveTo;
    
    public CreatePostcodeRuleRequest() {
    }
    
    public CreatePostcodeRuleRequest(String pattern, String level, String routeId, String effectiveFrom, String effectiveTo) {
        this.pattern = pattern;
        this.level = level;
        this.routeId = routeId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getRouteId() {
        return routeId;
    }
    
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    
    public String getEffectiveFrom() {
        return effectiveFrom;
    }
    
    public void setEffectiveFrom(String effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
    
    public String getEffectiveTo() {
        return effectiveTo;
    }
    
    public void setEffectiveTo(String effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
}

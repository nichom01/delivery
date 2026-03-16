package com.deliverysystem.dto;

public class PostcodeRuleDto {
    private String id;
    private String pattern;
    private String level; // 'full' | 'sector' | 'district' | 'area' | 'letter'
    private String routeId;
    private String routeName;
    private String effectiveFrom;
    private String effectiveTo;
    
    public PostcodeRuleDto() {
    }
    
    public PostcodeRuleDto(String id, String pattern, String level, String routeId, String routeName, String effectiveFrom, String effectiveTo) {
        this.id = id;
        this.pattern = pattern;
        this.level = level;
        this.routeId = routeId;
        this.routeName = routeName;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public String getRouteName() {
        return routeName;
    }
    
    public void setRouteName(String routeName) {
        this.routeName = routeName;
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

package com.deliverysystem.dto;

public class PostcodeHierarchyLevelDto {
    private String level;
    private String pattern;
    private String routeName;
    private Boolean isMatch;
    
    public PostcodeHierarchyLevelDto() {
    }
    
    public PostcodeHierarchyLevelDto(String level, String pattern, String routeName, Boolean isMatch) {
        this.level = level;
        this.pattern = pattern;
        this.routeName = routeName;
        this.isMatch = isMatch;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public String getRouteName() {
        return routeName;
    }
    
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
    
    public Boolean getIsMatch() {
        return isMatch;
    }
    
    public void setIsMatch(Boolean isMatch) {
        this.isMatch = isMatch;
    }
}

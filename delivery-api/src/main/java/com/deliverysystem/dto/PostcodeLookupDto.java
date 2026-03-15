package com.deliverysystem.dto;

import java.util.List;

public class PostcodeLookupDto {
    private List<PostcodeHierarchyLevelDto> hierarchy;
    
    public PostcodeLookupDto() {
    }
    
    public PostcodeLookupDto(List<PostcodeHierarchyLevelDto> hierarchy) {
        this.hierarchy = hierarchy;
    }
    
    public List<PostcodeHierarchyLevelDto> getHierarchy() {
        return hierarchy;
    }
    
    public void setHierarchy(List<PostcodeHierarchyLevelDto> hierarchy) {
        this.hierarchy = hierarchy;
    }
}

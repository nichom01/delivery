package com.deliverysystem.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateRouteRequest {
    @NotBlank(message = "Route code is required")
    private String code;
    
    @NotBlank(message = "Route name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Depot ID is required")
    private String depotId;
    
    public CreateRouteRequest() {
    }
    
    public CreateRouteRequest(String code, String name, String description, String depotId) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.depotId = depotId;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDepotId() {
        return depotId;
    }
    
    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }
}

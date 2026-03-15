package com.deliverysystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateManifestRequest {
    @NotBlank
    private String routeId;
    
    @NotBlank
    private String date;
    
    @NotBlank
    private String vehicleId;
    
    @NotBlank
    private String driverId;
    
    private List<String> boxIds;
    
    public CreateManifestRequest() {
    }
    
    public CreateManifestRequest(String routeId, String date, String vehicleId, String driverId, List<String> boxIds) {
        this.routeId = routeId;
        this.date = date;
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.boxIds = boxIds;
    }
    
    public String getRouteId() {
        return routeId;
    }
    
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public String getDriverId() {
        return driverId;
    }
    
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    
    public List<String> getBoxIds() {
        return boxIds;
    }
    
    public void setBoxIds(List<String> boxIds) {
        this.boxIds = boxIds;
    }
}

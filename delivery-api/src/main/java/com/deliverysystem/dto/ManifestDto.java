package com.deliverysystem.dto;

import java.util.List;

public class ManifestDto {
    private String id;
    private String routeId;
    private String date;
    private String driverId;
    private String vehicleId;
    private String status;
    private List<ManifestStopDto> stops;
    
    public ManifestDto() {
    }
    
    public ManifestDto(String id, String routeId, String date, String driverId, String vehicleId, String status, List<ManifestStopDto> stops) {
        this.id = id;
        this.routeId = routeId;
        this.date = date;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.status = status;
        this.stops = stops;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public String getDriverId() {
        return driverId;
    }
    
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    
    public String getVehicleId() {
        return vehicleId;
    }
    
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<ManifestStopDto> getStops() {
        return stops;
    }
    
    public void setStops(List<ManifestStopDto> stops) {
        this.stops = stops;
    }
}

package com.deliverysystem.dto;

public class UpdateManifestRequest {
    private String driverId;
    private String vehicleId;
    private String date;
    
    public UpdateManifestRequest() {
    }
    
    public UpdateManifestRequest(String driverId, String vehicleId, String date) {
        this.driverId = driverId;
        this.vehicleId = vehicleId;
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
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
}

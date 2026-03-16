package com.deliverysystem.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDepotRequest {
    
    @NotBlank(message = "Depot name is required")
    private String name;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String latitude;
    private String longitude;
    
    public CreateDepotRequest() {
    }
    
    public CreateDepotRequest(String name, String address, String latitude, String longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getLatitude() {
        return latitude;
    }
    
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    
    public String getLongitude() {
        return longitude;
    }
    
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

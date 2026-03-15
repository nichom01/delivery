package com.deliverysystem.dto;

public class DepotDto {
    private String id;
    private String name;
    private String location;
    private Integer routesCount;
    private Integer vehiclesCount;
    private Integer driversCount;
    private String status;
    
    public DepotDto() {
    }
    
    public DepotDto(String id, String name, String location, Integer routesCount, Integer vehiclesCount, Integer driversCount, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.routesCount = routesCount;
        this.vehiclesCount = vehiclesCount;
        this.driversCount = driversCount;
        this.status = status;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Integer getRoutesCount() {
        return routesCount;
    }
    
    public void setRoutesCount(Integer routesCount) {
        this.routesCount = routesCount;
    }
    
    public Integer getVehiclesCount() {
        return vehiclesCount;
    }
    
    public void setVehiclesCount(Integer vehiclesCount) {
        this.vehiclesCount = vehiclesCount;
    }
    
    public Integer getDriversCount() {
        return driversCount;
    }
    
    public void setDriversCount(Integer driversCount) {
        this.driversCount = driversCount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

package com.deliverysystem.dto;

import java.util.List;

public class RouteDrilldownDto {
    private String routeName;
    private String vehicle;
    private String driver;
    private RouteStatsDto stats;
    private List<DeliveryStopDto> stops;
    
    public RouteDrilldownDto() {
    }
    
    public RouteDrilldownDto(String routeName, String vehicle, String driver, RouteStatsDto stats, List<DeliveryStopDto> stops) {
        this.routeName = routeName;
        this.vehicle = vehicle;
        this.driver = driver;
        this.stats = stats;
        this.stops = stops;
    }
    
    public String getRouteName() {
        return routeName;
    }
    
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
    
    public String getVehicle() {
        return vehicle;
    }
    
    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
    
    public String getDriver() {
        return driver;
    }
    
    public void setDriver(String driver) {
        this.driver = driver;
    }
    
    public RouteStatsDto getStats() {
        return stats;
    }
    
    public void setStats(RouteStatsDto stats) {
        this.stats = stats;
    }
    
    public List<DeliveryStopDto> getStops() {
        return stops;
    }
    
    public void setStops(List<DeliveryStopDto> stops) {
        this.stops = stops;
    }
}

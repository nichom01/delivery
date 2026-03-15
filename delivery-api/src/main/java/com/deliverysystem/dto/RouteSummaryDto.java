package com.deliverysystem.dto;

public class RouteSummaryDto {
    private String routeId;
    private String routeName;
    private String description;
    private String vehicle;
    private String driver;
    private Integer deliveriesDone;
    private Integer deliveriesTotal;
    private Integer boxesDone;
    private Integer boxesTotal;
    private String status;
    private String progressNote;
    
    public RouteSummaryDto() {
    }
    
    public RouteSummaryDto(String routeId, String routeName, String description, String vehicle, String driver, Integer deliveriesDone, Integer deliveriesTotal, Integer boxesDone, Integer boxesTotal, String status, String progressNote) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.description = description;
        this.vehicle = vehicle;
        this.driver = driver;
        this.deliveriesDone = deliveriesDone;
        this.deliveriesTotal = deliveriesTotal;
        this.boxesDone = boxesDone;
        this.boxesTotal = boxesTotal;
        this.status = status;
        this.progressNote = progressNote;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public Integer getDeliveriesDone() {
        return deliveriesDone;
    }
    
    public void setDeliveriesDone(Integer deliveriesDone) {
        this.deliveriesDone = deliveriesDone;
    }
    
    public Integer getDeliveriesTotal() {
        return deliveriesTotal;
    }
    
    public void setDeliveriesTotal(Integer deliveriesTotal) {
        this.deliveriesTotal = deliveriesTotal;
    }
    
    public Integer getBoxesDone() {
        return boxesDone;
    }
    
    public void setBoxesDone(Integer boxesDone) {
        this.boxesDone = boxesDone;
    }
    
    public Integer getBoxesTotal() {
        return boxesTotal;
    }
    
    public void setBoxesTotal(Integer boxesTotal) {
        this.boxesTotal = boxesTotal;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getProgressNote() {
        return progressNote;
    }
    
    public void setProgressNote(String progressNote) {
        this.progressNote = progressNote;
    }
}

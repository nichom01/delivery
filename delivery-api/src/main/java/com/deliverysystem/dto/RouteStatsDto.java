package com.deliverysystem.dto;

public class RouteStatsDto {
    private Integer deliveriesDone;
    private Integer deliveriesTotal;
    private Integer boxesDone;
    private Integer boxesTotal;
    private Integer exceptionsCount;
    private String lastActivity;
    private String lastActivityPostcode;
    
    public RouteStatsDto() {
    }
    
    public RouteStatsDto(Integer deliveriesDone, Integer deliveriesTotal, Integer boxesDone, Integer boxesTotal, Integer exceptionsCount, String lastActivity, String lastActivityPostcode) {
        this.deliveriesDone = deliveriesDone;
        this.deliveriesTotal = deliveriesTotal;
        this.boxesDone = boxesDone;
        this.boxesTotal = boxesTotal;
        this.exceptionsCount = exceptionsCount;
        this.lastActivity = lastActivity;
        this.lastActivityPostcode = lastActivityPostcode;
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
    
    public Integer getExceptionsCount() {
        return exceptionsCount;
    }
    
    public void setExceptionsCount(Integer exceptionsCount) {
        this.exceptionsCount = exceptionsCount;
    }
    
    public String getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public String getLastActivityPostcode() {
        return lastActivityPostcode;
    }
    
    public void setLastActivityPostcode(String lastActivityPostcode) {
        this.lastActivityPostcode = lastActivityPostcode;
    }
}

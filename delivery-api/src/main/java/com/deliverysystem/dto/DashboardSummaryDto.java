package com.deliverysystem.dto;

public class DashboardSummaryDto {
    private Integer totalRoutes;
    private Integer deliveriesComplete;
    private Integer deliveriesTotal;
    private Integer boxesDelivered;
    private Integer boxesTotal;
    private Integer exceptionsCount;
    
    public DashboardSummaryDto() {
    }
    
    public DashboardSummaryDto(Integer totalRoutes, Integer deliveriesComplete, Integer deliveriesTotal, Integer boxesDelivered, Integer boxesTotal, Integer exceptionsCount) {
        this.totalRoutes = totalRoutes;
        this.deliveriesComplete = deliveriesComplete;
        this.deliveriesTotal = deliveriesTotal;
        this.boxesDelivered = boxesDelivered;
        this.boxesTotal = boxesTotal;
        this.exceptionsCount = exceptionsCount;
    }
    
    public Integer getTotalRoutes() {
        return totalRoutes;
    }
    
    public void setTotalRoutes(Integer totalRoutes) {
        this.totalRoutes = totalRoutes;
    }
    
    public Integer getDeliveriesComplete() {
        return deliveriesComplete;
    }
    
    public void setDeliveriesComplete(Integer deliveriesComplete) {
        this.deliveriesComplete = deliveriesComplete;
    }
    
    public Integer getDeliveriesTotal() {
        return deliveriesTotal;
    }
    
    public void setDeliveriesTotal(Integer deliveriesTotal) {
        this.deliveriesTotal = deliveriesTotal;
    }
    
    public Integer getBoxesDelivered() {
        return boxesDelivered;
    }
    
    public void setBoxesDelivered(Integer boxesDelivered) {
        this.boxesDelivered = boxesDelivered;
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
}

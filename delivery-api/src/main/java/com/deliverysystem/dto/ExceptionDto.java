package com.deliverysystem.dto;

public class ExceptionDto {
    private String orderId;
    private String boxesSummary;
    private String routeName;
    
    public ExceptionDto() {
    }
    
    public ExceptionDto(String orderId, String boxesSummary, String routeName) {
        this.orderId = orderId;
        this.boxesSummary = boxesSummary;
        this.routeName = routeName;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getBoxesSummary() {
        return boxesSummary;
    }
    
    public void setBoxesSummary(String boxesSummary) {
        this.boxesSummary = boxesSummary;
    }
    
    public String getRouteName() {
        return routeName;
    }
    
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
}

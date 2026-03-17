package com.deliverysystem.dto;

import java.util.List;

public class OrderAwaitingGoodsDto {
    private String id;
    private String orderId;
    private String customer;
    private String routeName;
    private Integer boxesReceived;
    private Integer boxesExpected;
    private List<BoxDto> boxes;

    public OrderAwaitingGoodsDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getCustomer() {
        return customer;
    }
    
    public void setCustomer(String customer) {
        this.customer = customer;
    }
    
    public String getRouteName() {
        return routeName;
    }
    
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
    
    public Integer getBoxesReceived() {
        return boxesReceived;
    }
    
    public void setBoxesReceived(Integer boxesReceived) {
        this.boxesReceived = boxesReceived;
    }
    
    public Integer getBoxesExpected() {
        return boxesExpected;
    }
    
    public void setBoxesExpected(Integer boxesExpected) {
        this.boxesExpected = boxesExpected;
    }
    
    public List<BoxDto> getBoxes() {
        return boxes;
    }
    
    public void setBoxes(List<BoxDto> boxes) {
        this.boxes = boxes;
    }
}

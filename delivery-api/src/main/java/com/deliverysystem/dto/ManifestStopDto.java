package com.deliverysystem.dto;

public class ManifestStopDto {
    private String orderId;
    private String address;
    private Object boxes; // Can be number or string
    private String boxStatus;
    
    public ManifestStopDto() {
    }
    
    public ManifestStopDto(String orderId, String address, Object boxes, String boxStatus) {
        this.orderId = orderId;
        this.address = address;
        this.boxes = boxes;
        this.boxStatus = boxStatus;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Object getBoxes() {
        return boxes;
    }
    
    public void setBoxes(Object boxes) {
        this.boxes = boxes;
    }
    
    public String getBoxStatus() {
        return boxStatus;
    }
    
    public void setBoxStatus(String boxStatus) {
        this.boxStatus = boxStatus;
    }
}

package com.deliverysystem.dto;

public class DeliveryStopDto {
    private Integer seq;
    private String address;
    private String postcode;
    private Object boxes; // Can be number or string
    private String status;
    private String deliveryTime;
    private Boolean hasPod;
    
    public DeliveryStopDto() {
    }
    
    public DeliveryStopDto(Integer seq, String address, String postcode, Object boxes, String status, String deliveryTime, Boolean hasPod) {
        this.seq = seq;
        this.address = address;
        this.postcode = postcode;
        this.boxes = boxes;
        this.status = status;
        this.deliveryTime = deliveryTime;
        this.hasPod = hasPod;
    }
    
    public Integer getSeq() {
        return seq;
    }
    
    public void setSeq(Integer seq) {
        this.seq = seq;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPostcode() {
        return postcode;
    }
    
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
    
    public Object getBoxes() {
        return boxes;
    }
    
    public void setBoxes(Object boxes) {
        this.boxes = boxes;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDeliveryTime() {
        return deliveryTime;
    }
    
    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    
    public Boolean getHasPod() {
        return hasPod;
    }
    
    public void setHasPod(Boolean hasPod) {
        this.hasPod = hasPod;
    }
}

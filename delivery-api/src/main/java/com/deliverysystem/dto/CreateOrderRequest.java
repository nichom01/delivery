package com.deliverysystem.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CreateOrderRequest {
    @NotBlank
    private String orderId;
    
    @NotBlank
    private String despatchId;
    
    private String customerAddress;
    
    @NotBlank
    private String deliveryPostcode;
    
    private String orderDate;
    
    private String requestedDeliveryDate;
    
    private List<String> boxIdentifiers;
    
    private Integer expectedBoxCount;
    
    public CreateOrderRequest() {
    }
    
    public CreateOrderRequest(String orderId, String despatchId, String customerAddress, String deliveryPostcode, String orderDate, String requestedDeliveryDate, List<String> boxIdentifiers, Integer expectedBoxCount) {
        this.orderId = orderId;
        this.despatchId = despatchId;
        this.customerAddress = customerAddress;
        this.deliveryPostcode = deliveryPostcode;
        this.orderDate = orderDate;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.boxIdentifiers = boxIdentifiers;
        this.expectedBoxCount = expectedBoxCount;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getDespatchId() {
        return despatchId;
    }
    
    public void setDespatchId(String despatchId) {
        this.despatchId = despatchId;
    }
    
    public String getCustomerAddress() {
        return customerAddress;
    }
    
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
    
    public String getDeliveryPostcode() {
        return deliveryPostcode;
    }
    
    public void setDeliveryPostcode(String deliveryPostcode) {
        this.deliveryPostcode = deliveryPostcode;
    }
    
    public String getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
    
    public String getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }
    
    public void setRequestedDeliveryDate(String requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }
    
    public List<String> getBoxIdentifiers() {
        return boxIdentifiers;
    }
    
    public void setBoxIdentifiers(List<String> boxIdentifiers) {
        this.boxIdentifiers = boxIdentifiers;
    }
    
    public Integer getExpectedBoxCount() {
        return expectedBoxCount;
    }
    
    public void setExpectedBoxCount(Integer expectedBoxCount) {
        this.expectedBoxCount = expectedBoxCount;
    }
}

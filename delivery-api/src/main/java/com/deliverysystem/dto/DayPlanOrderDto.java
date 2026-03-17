package com.deliverysystem.dto;

public class DayPlanOrderDto {

    private String id;
    private String orderId;
    private String customerAddress;
    private String deliveryPostcode;
    private String orderStatus;
    private int totalBoxes;
    private int boxesExpected;
    private int boxesReceived;
    private int boxesReady;

    public DayPlanOrderDto() {
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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getTotalBoxes() {
        return totalBoxes;
    }

    public void setTotalBoxes(int totalBoxes) {
        this.totalBoxes = totalBoxes;
    }

    public int getBoxesExpected() {
        return boxesExpected;
    }

    public void setBoxesExpected(int boxesExpected) {
        this.boxesExpected = boxesExpected;
    }

    public int getBoxesReceived() {
        return boxesReceived;
    }

    public void setBoxesReceived(int boxesReceived) {
        this.boxesReceived = boxesReceived;
    }

    public int getBoxesReady() {
        return boxesReady;
    }

    public void setBoxesReady(int boxesReady) {
        this.boxesReady = boxesReady;
    }
}

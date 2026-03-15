package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"order_id", "despatch_id"})
})
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @Column(name = "despatch_id", nullable = false)
    private String despatchId;
    
    @Column(name = "customer_address")
    private String customerAddress;
    
    @Column(name = "delivery_postcode", nullable = false)
    private String deliveryPostcode;
    
    @Column(name = "order_date")
    private LocalDate orderDate;
    
    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Box> boxes = new ArrayList<>();
    
    @Column(nullable = false)
    private String status = "PENDING";
    
    public Order() {
    }
    
    public Order(String id, String orderId, String despatchId, String customerAddress, String deliveryPostcode, LocalDate orderDate, LocalDate requestedDeliveryDate, Route route, List<Box> boxes, String status) {
        this.id = id;
        this.orderId = orderId;
        this.despatchId = despatchId;
        this.customerAddress = customerAddress;
        this.deliveryPostcode = deliveryPostcode;
        this.orderDate = orderDate;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.route = route;
        this.boxes = boxes;
        this.status = status;
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
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDate getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }
    
    public void setRequestedDeliveryDate(LocalDate requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }
    
    public Route getRoute() {
        return route;
    }
    
    public void setRoute(Route route) {
        this.route = route;
    }
    
    public List<Box> getBoxes() {
        return boxes;
    }
    
    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}

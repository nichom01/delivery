package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "boxes")
public class Box {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String identifier; // Box identifier from source system
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoxStatus status = BoxStatus.EXPECTED;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id")
    private Manifest manifest;
    
    public Box() {
    }
    
    public Box(String id, String identifier, BoxStatus status, LocalDateTime receivedAt, Order order, Manifest manifest) {
        this.id = id;
        this.identifier = identifier;
        this.status = status;
        this.receivedAt = receivedAt;
        this.order = order;
        this.manifest = manifest;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public BoxStatus getStatus() {
        return status;
    }
    
    public void setStatus(BoxStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Manifest getManifest() {
        return manifest;
    }
    
    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }
    
    public enum BoxStatus {
        EXPECTED,
        RECEIVED,
        MANIFESTED,
        DELIVERED,
        EXCEPTION
    }
}

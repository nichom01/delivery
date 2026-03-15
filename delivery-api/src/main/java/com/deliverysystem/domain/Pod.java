package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pods")
public class Pod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "manifest_id", nullable = false)
    private String manifestId;
    
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @Column(name = "delivery_address")
    private String deliveryAddress;
    
    @Column(name = "postcode")
    private String postcode;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @Column(nullable = false)
    private LocalDateTime timestamp; // Timestamp from device at capture time
    
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt; // Timestamp when uploaded to server
    
    public Pod() {
    }
    
    public Pod(String id, String manifestId, String orderId, String deliveryAddress, String postcode, String imageUrl, LocalDateTime timestamp, LocalDateTime uploadedAt) {
        this.id = id;
        this.manifestId = manifestId;
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.postcode = postcode;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.uploadedAt = uploadedAt;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getManifestId() {
        return manifestId;
    }
    
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getPostcode() {
        return postcode;
    }
    
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}

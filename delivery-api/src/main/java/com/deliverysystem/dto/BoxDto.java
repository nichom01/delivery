package com.deliverysystem.dto;

public class BoxDto {
    private String id;
    private String status; // 'received' | 'pending' | 'missing'
    private String receivedAt;
    
    public BoxDto() {
    }
    
    public BoxDto(String id, String status, String receivedAt) {
        this.id = id;
        this.status = status;
        this.receivedAt = receivedAt;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getReceivedAt() {
        return receivedAt;
    }
    
    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }
}

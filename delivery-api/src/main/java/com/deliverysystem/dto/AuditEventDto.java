package com.deliverysystem.dto;

public class AuditEventDto {
    private String timestamp;
    private String userId;
    private String userName;
    private String role;
    private String action; // 'CREATE' | 'UPDATE' | 'DELETE'
    private String entityType;
    private String detail;
    
    public AuditEventDto() {
    }
    
    public AuditEventDto(String timestamp, String userId, String userName, String role, String action, String entityType, String detail) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.action = action;
        this.entityType = entityType;
        this.detail = detail;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String detail) {
        this.detail = detail;
    }
}

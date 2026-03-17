package com.deliverysystem.dto;

public class AuditEventDto {
    private String timestamp;
    private String userId;
    private String userName;
    private String role;
    private String action; // 'CREATE' | 'UPDATE' | 'DELETE'
    private String entityType;
    private String entityId;
    private String beforeValue;
    private String afterValue;
    private String detail;
    
    public AuditEventDto() {
    }
    
    public AuditEventDto(String timestamp, String userId, String userName, String role, String action, String entityType, String entityId, String beforeValue, String afterValue, String detail) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
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
    
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public String getBeforeValue() {
        return beforeValue;
    }
    
    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }
    
    public String getAfterValue() {
        return afterValue;
    }
    
    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String detail) {
        this.detail = detail;
    }
}

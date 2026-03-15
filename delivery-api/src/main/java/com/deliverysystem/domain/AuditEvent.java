package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_depot", columnList = "depot_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id")
})
public class AuditEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(nullable = false)
    private String role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "entity_id")
    private String entityId;
    
    @Column(name = "depot_id")
    private String depotId;
    
    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;
    
    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;
    
    @Column(columnDefinition = "TEXT")
    private String detail;
    
    public AuditEvent() {
    }
    
    public AuditEvent(String id, LocalDateTime timestamp, String userId, String userName, String role, AuditAction action, String entityType, String entityId, String depotId, String beforeValue, String afterValue, String detail) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.depotId = depotId;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.detail = detail;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
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
    
    public AuditAction getAction() {
        return action;
    }
    
    public void setAction(AuditAction action) {
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
    
    public String getDepotId() {
        return depotId;
    }
    
    public void setDepotId(String depotId) {
        this.depotId = depotId;
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
    
    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE
    }
}

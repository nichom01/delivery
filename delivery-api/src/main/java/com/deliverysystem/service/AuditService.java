package com.deliverysystem.service;

import com.deliverysystem.domain.AuditEvent;
import com.deliverysystem.domain.User;
import com.deliverysystem.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {
    
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository auditEventRepository;
    
    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }
    
    @Transactional
    public void logCreate(User user, String entityType, String entityId, String depotId, Object entity) {
        AuditEvent event = createAuditEvent(user, AuditEvent.AuditAction.CREATE, entityType, entityId, depotId, null, entity);
        auditEventRepository.save(event);
    }
    
    @Transactional
    public void logUpdate(User user, String entityType, String entityId, String depotId, Object before, Object after) {
        AuditEvent event = createAuditEvent(user, AuditEvent.AuditAction.UPDATE, entityType, entityId, depotId, before, after);
        auditEventRepository.save(event);
    }
    
    @Transactional
    public void logDelete(User user, String entityType, String entityId, String depotId, Object entity) {
        AuditEvent event = createAuditEvent(user, AuditEvent.AuditAction.DELETE, entityType, entityId, depotId, entity, null);
        auditEventRepository.save(event);
    }
    
    private AuditEvent createAuditEvent(User user, AuditEvent.AuditAction action, String entityType, 
                                       String entityId, String depotId, Object before, Object after) {
        AuditEvent event = new AuditEvent();
        event.setTimestamp(LocalDateTime.now());
        event.setUserId(user.getId());
        event.setUserName(user.getName());
        event.setRole(user.getRole().name());
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setDepotId(depotId);
        event.setBeforeValue(before != null ? before.toString() : null);
        event.setAfterValue(after != null ? after.toString() : null);
        return event;
    }
}

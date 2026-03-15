package com.deliverysystem.repository;

import com.deliverysystem.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    List<AuditEvent> findByDepotIdOrderByTimestampDesc(String depotId);
    List<AuditEvent> findByDepotIdAndTimestampBetweenOrderByTimestampDesc(
        String depotId, LocalDateTime start, LocalDateTime end);
    List<AuditEvent> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
}

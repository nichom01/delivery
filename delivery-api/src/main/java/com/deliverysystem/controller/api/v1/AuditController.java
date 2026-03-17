package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.AuditEvent;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.AuditEventDto;
import com.deliverysystem.repository.AuditEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {
    
    private final AuditEventRepository auditEventRepository;
    
    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditEventDto>>> getAuditEvents(
            @RequestParam(required = false) String depotId) {
        List<AuditEvent> events;
        if (depotId != null) {
            events = auditEventRepository.findByDepotIdOrderByTimestampDesc(depotId);
        } else {
            events = auditEventRepository.findAll();
        }
        
        List<AuditEventDto> dtos = events.stream().map(event -> {
            AuditEventDto dto = new AuditEventDto();
            dto.setTimestamp(event.getTimestamp().format(DATE_TIME_FORMATTER));
            dto.setUserId(event.getUserId());
            dto.setUserName(event.getUserName());
            dto.setRole(event.getRole());
            dto.setAction(event.getAction().name());
            dto.setEntityType(event.getEntityType());
            dto.setEntityId(event.getEntityId());
            dto.setBeforeValue(event.getBeforeValue());
            dto.setAfterValue(event.getAfterValue());
            dto.setDetail(event.getDetail());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}

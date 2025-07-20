package com.store.mgmt.users.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.config.TenantContext;
import com.store.mgmt.users.model.entity.AuditLog;
import com.store.mgmt.users.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogServiceImpl implements AuditLogService{

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, UUID entityId, String details) {
        try {
            Map<String, Object> detailsMap = objectMapper.readValue(details, HashMap.class);
            AuditLog log = new AuditLog();
            log.setAction(action);
            log.setEntityType(entityId.getClass().getSimpleName());
            log.setEntityId(entityId);
            log.setDetails(detailsMap.toString());
            log.setOrganization(TenantContext.getCurrentOrganization());
            log.setStore(TenantContext.getCurrentStore());
            log.setUser(TenantContext.getCurrentUser());
            auditLogRepository.save(log);
        } catch (Exception e) {
            throw new RuntimeException("Failed to log audit entry", e);
        }
    }
}

package com.store.mgmt.users.service;

import com.store.mgmt.config.TenantContext;
import com.store.mgmt.users.model.entity.AuditLog;
import com.store.mgmt.users.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditLogServiceImpl implements AuditLogService{

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, UUID entityId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityId.getClass().getSimpleName());
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setOrganization(TenantContext.getCurrentOrganization());
        log.setStore(TenantContext.getCurrentStore());
        log.setUser(TenantContext.getCurrentUser());
        auditLogRepository.save(log);
    }
}

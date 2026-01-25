package com.store.mgmt.shared.infrastructure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of AuditLogService for persisting audit entries.
 * Part of shared infrastructure used across modules.
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AuditLogBuilder builder() {
        return new AuditLogBuilderImpl(this);
    }

    @Override
    @Transactional
    public void persistAuditLog(String action, UUID entityId, Map<String, Object> detailsData) {
        try {
            logger.debug("Creating audit entry: action={}, entityId={}", action, entityId);

            AuditLog log = new AuditLog();
            log.setAction(action);
            log.setEntityType(entityId != null ? "UUID" : "Unknown");
            log.setEntityId(entityId);

            Map<String, Object> finalDetails = new HashMap<>(detailsData);
            finalDetails.put("correlationId", MDC.get("correlationId"));

            log.setDetails(objectMapper.writeValueAsString(finalDetails));

            // Get tenant info from TenantContext
            TenantContext.currentOptional().ifPresent(ctx -> {
                log.setOrganizationId(ctx.organizationId());
                log.setStoreId(ctx.storeId());
                log.setUserId(ctx.userId());
                log.setUsername(ctx.username());
            });

            auditLogRepository.save(log);
            logger.debug("Audit entry persisted: action={}, entityId={}", action, entityId);
        } catch (Exception e) {
            // Log the error but don't fail - audit logging should be non-blocking
            logger.error("Failed to persist audit entry: action={}, entityId={}", action, entityId, e);
        }
    }

    /**
     * Builder implementation for creating audit log entries fluently.
     */
    private static class AuditLogBuilderImpl implements AuditLogBuilder {
        private final AuditLogServiceImpl service;
        private String action;
        private UUID entityId;
        private final Map<String, Object> details = new HashMap<>();

        private AuditLogBuilderImpl(AuditLogServiceImpl service) {
            this.service = service;
        }

        @Override
        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        @Override
        public AuditLogBuilder entityId(UUID entityId) {
            this.entityId = entityId;
            return this;
        }

        @Override
        public AuditLogBuilder message(String message) {
            this.details.put("message", message);
            return this;
        }

        @Override
        public AuditLogBuilder storeName(String storeName) {
            this.details.put("storeName", storeName);
            return this;
        }

        @Override
        public AuditLogBuilder organizationName(String orgName) {
            this.details.put("organizationName", orgName);
            return this;
        }

        @Override
        public AuditLogBuilder oldValue(Object oldValue) {
            this.details.put("oldValue", oldValue);
            return this;
        }

        @Override
        public AuditLogBuilder newValue(Object newValue) {
            this.details.put("newValue", newValue);
            return this;
        }

        @Override
        public AuditLogBuilder detail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        @Override
        public void log() {
            if (this.action == null) {
                logger.warn("Audit log requires 'action'. Skipping log entry.");
                return;
            }
            service.persistAuditLog(this.action, this.entityId, this.details);
        }
    }
}

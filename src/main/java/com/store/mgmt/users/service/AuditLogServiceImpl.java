package com.store.mgmt.users.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.mgmt.config.TenantContext;
import com.store.mgmt.users.model.entity.AuditLog;
import com.store.mgmt.users.repository.AuditLogRepository;
import jakarta.transaction.Transactional;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogServiceImpl implements AuditLogService{

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuditLogServiceImpl.class);
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    /**
     * Entry point to create a new AuditLogBuilder.
     * This is the method you'll call from your business logic.
     */
    public AuditLogBuilder builder() {
        return new AuditLogBuilder(this); // Pass a reference to the service itself
    }

    @Transactional()
    public void _persistAuditLog(String action, UUID entityId, Map<String, Object> detailsData) {
        try {
            logger.debug("Creating audit entry: action={}, entityId={}", action, entityId);

            AuditLog log = new AuditLog();
            log.setAction(action);
            log.setEntityType(entityId != null ? entityId.getClass().getSimpleName() : "Unknown");
            log.setEntityId(entityId);

            Map<String, Object> finalDetails = new HashMap<>(detailsData);
            finalDetails.put("correlationId", MDC.get("correlationId"));

            log.setDetails(objectMapper.writeValueAsString(finalDetails));
            log.setOrganization(TenantContext.getCurrentOrganization());
            log.setStore(TenantContext.getCurrentStore());
            log.setUser(TenantContext.getCurrentUser());

            auditLogRepository.save(log);
            logger.debug("Audit entry persisted: action={}, entityId={}", action, entityId);
        } catch (Exception e) {
            // Log the error but don't fail - audit logging should be non-blocking
            logger.error("Failed to persist audit entry: action={}, entityId={}", action, entityId, e);
        }
    }


        public static class AuditLogBuilder {
            private final AuditLogServiceImpl service; // Reference to the outer service
            private String action;
            private UUID entityId;
            private Map<String, Object> details = new HashMap<>(); // Use a Map to build details

            private AuditLogBuilder(AuditLogServiceImpl service) {
                this.service = service;
            }

            public AuditLogBuilder action(String action) {
                this.action = action;
                return this;
            }

            public AuditLogBuilder entityId(UUID entityId) {
                this.entityId = entityId;
                return this;
            }

            public AuditLogBuilder message(String message) {
                this.details.put("message", message);
                return this;
            }

            // Specific detail setters
            public AuditLogBuilder storeName(String storeName) {
                this.details.put("storeName", storeName);
                return this;
            }

            public AuditLogBuilder organizationName(String orgName) {
                this.details.put("organizationName", orgName);
                return this;
            }

            public AuditLogBuilder oldValue(Object oldValue) {
                this.details.put("oldValue", oldValue);
                return this;
            }

            public AuditLogBuilder newValue(Object newValue) {
                this.details.put("newValue", newValue);
                return this;
            }

            // Generic detail setter for ad-hoc fields
            public AuditLogBuilder detail(String key, Object value) {
                this.details.put(key, value);
                return this;
            }

            /**
             * Final method to build the audit log and trigger persistence.
             */
            public void log() {
                if (this.action == null) {
                    logger.warn("Audit log requires 'action' . Skipping log entry.");
                    return;
                }
                // Delegate to the actual service method for persistence and transaction management
                service._persistAuditLog(this.action, this.entityId, this.details);
            }
    }
}

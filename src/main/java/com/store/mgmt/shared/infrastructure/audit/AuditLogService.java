package com.store.mgmt.shared.infrastructure.audit;

import java.util.Map;
import java.util.UUID;

/**
 * AuditLogService interface defines the contract for audit logging operations.
 * Part of shared infrastructure used across modules.
 */
public interface AuditLogService {

    /**
     * Logs an action performed on an entity.
     *
     * @param action    The action performed (e.g., "CREATE", "UPDATE", "DELETE").
     * @param entityId  The unique identifier of the entity.
     * @param details   Additional details about the action.
     */
    void persistAuditLog(String action, UUID entityId, Map<String, Object> details);

    /**
     * Creates a new AuditLogBuilder instance for building audit log entries.
     *
     * @return A new AuditLogBuilder instance.
     */
    AuditLogBuilder builder();

    /**
     * Builder class for creating audit log entries fluently.
     */
    interface AuditLogBuilder {
        AuditLogBuilder action(String action);
        AuditLogBuilder entityId(UUID entityId);
        AuditLogBuilder message(String message);
        AuditLogBuilder storeName(String storeName);
        AuditLogBuilder organizationName(String orgName);
        AuditLogBuilder oldValue(Object oldValue);
        AuditLogBuilder newValue(Object newValue);
        AuditLogBuilder detail(String key, Object value);
        void log();
    }
}

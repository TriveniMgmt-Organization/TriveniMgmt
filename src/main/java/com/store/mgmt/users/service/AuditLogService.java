package com.store.mgmt.users.service;

import com.store.mgmt.users.model.dto.CreateUserDTO;
import com.store.mgmt.users.model.dto.UpdateUserDTO;
import com.store.mgmt.users.model.dto.UserDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UserService interface defines the contract for user management operations.
 * It includes methods for creating, retrieving, updating, and deleting users,
 * as well as assigning and removing roles from users.
 */
public interface AuditLogService {

    /**
     * Logs an action performed on a user entity.
     *
     * @param action    The action performed (e.g., "CREATE", "UPDATE", "DELETE").
     * @param entityId  The unique identifier of the user entity.
     * @param details   Additional details about the action.
     */
    void _persistAuditLog(String action, UUID entityId, Map<String, Object> details);

    /**
     * Creates a new AuditLogBuilder instance for building audit log entries.
     *
     * @return A new AuditLogBuilder instance.
     */
    AuditLogServiceImpl.AuditLogBuilder builder();

}
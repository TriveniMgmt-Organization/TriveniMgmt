package com.store.mgmt.modules.users.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a role is not found.
 */
public class RoleNotFoundException extends RuntimeException {

    private final UUID roleId;

    public RoleNotFoundException(UUID roleId) {
        super("Role not found: " + roleId);
        this.roleId = roleId;
    }

    public UUID getRoleId() {
        return roleId;
    }
}

package com.store.mgmt.modules.users.domain.exception;

import com.store.mgmt.modules.users.domain.model.RoleId;

/**
 * Exception thrown when a role is not found.
 */
public class RoleNotFoundException extends RuntimeException {

    private final RoleId roleId;

    public RoleNotFoundException(RoleId roleId) {
        super("Role not found: " + roleId.getValue());
        this.roleId = roleId;
    }

    public RoleId getRoleId() {
        return roleId;
    }
}

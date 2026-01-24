package com.store.mgmt.modules.users.domain.event;

import com.store.mgmt.modules.users.domain.model.PermissionId;
import com.store.mgmt.modules.users.domain.model.RoleId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a permission is added to or removed from a role.
 */
public class RolePermissionChanged extends BaseDomainEvent {

    private final RoleId roleId;
    private final PermissionId permissionId;
    private final boolean added;

    public RolePermissionChanged(RoleId roleId, PermissionId permissionId, boolean added) {
        super(roleId.getValue(), "Role");
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.added = added;
    }

    public RoleId getRoleId() {
        return roleId;
    }

    public PermissionId getPermissionId() {
        return permissionId;
    }

    public boolean isAdded() {
        return added;
    }

    public boolean isRemoved() {
        return !added;
    }
}

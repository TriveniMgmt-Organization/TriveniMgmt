package com.store.mgmt.modules.users.domain.event;

import com.store.mgmt.modules.users.domain.model.RoleId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a new role is created.
 */
public class RoleCreated extends BaseDomainEvent {

    private final RoleId roleId;
    private final String name;

    public RoleCreated(RoleId roleId, String name) {
        super(roleId.getValue(), "Role");
        this.roleId = roleId;
        this.name = name;
    }

    public RoleId getRoleId() {
        return roleId;
    }

    public String getName() {
        return name;
    }
}

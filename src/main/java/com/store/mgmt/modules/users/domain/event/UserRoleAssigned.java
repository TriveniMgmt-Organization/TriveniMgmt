package com.store.mgmt.modules.users.domain.event;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.modules.users.domain.model.RoleId;
import com.store.mgmt.modules.users.domain.model.UserId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a role is assigned to a user.
 */
public class UserRoleAssigned extends BaseDomainEvent {

    private final UserId userId;
    private final RoleId roleId;
    private final OrganizationId organizationId;
    private final StoreId storeId; // nullable

    public UserRoleAssigned(UserId userId, RoleId roleId, OrganizationId organizationId, StoreId storeId) {
        super(userId.getValue(), "User");
        this.userId = userId;
        this.roleId = roleId;
        this.organizationId = organizationId;
        this.storeId = storeId;
    }

    public UserId getUserId() {
        return userId;
    }

    public RoleId getRoleId() {
        return roleId;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public StoreId getStoreId() {
        return storeId;
    }
}

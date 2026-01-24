package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.StoreId;

import java.util.Objects;

/**
 * Value object representing a user's role assignment within an organization and optionally a store.
 */
public record UserOrganizationRole(
        RoleId roleId,
        OrganizationId organizationId,
        StoreId storeId  // nullable - if null, role applies to entire organization
) {
    public UserOrganizationRole {
        Objects.requireNonNull(roleId, "Role ID is required");
        Objects.requireNonNull(organizationId, "Organization ID is required");
        // storeId can be null
    }

    /**
     * Check if this role is organization-wide (not store-specific).
     */
    public boolean isOrganizationWide() {
        return storeId == null;
    }

    /**
     * Check if this role is store-specific.
     */
    public boolean isStoreSpecific() {
        return storeId != null;
    }
}

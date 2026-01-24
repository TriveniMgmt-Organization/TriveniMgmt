package com.store.mgmt.modules.organization.domain.event;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when an organization is updated.
 */
public final class OrganizationUpdated extends BaseDomainEvent {

    private final OrganizationId organizationId;
    private final String updatedFields;

    public OrganizationUpdated(OrganizationId organizationId, String updatedFields) {
        super(organizationId.getValue(), "Organization");
        this.organizationId = organizationId;
        this.updatedFields = updatedFields;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getUpdatedFields() {
        return updatedFields;
    }
}

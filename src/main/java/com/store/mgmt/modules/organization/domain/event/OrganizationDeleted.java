package com.store.mgmt.modules.organization.domain.event;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when an organization is soft-deleted.
 */
public final class OrganizationDeleted extends BaseDomainEvent {

    private final OrganizationId organizationId;

    public OrganizationDeleted(OrganizationId organizationId) {
        super(organizationId.getValue(), "Organization");
        this.organizationId = organizationId;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }
}

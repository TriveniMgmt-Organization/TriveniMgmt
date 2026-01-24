package com.store.mgmt.modules.organization.domain.event;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.UserId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a new organization is created.
 */
public final class OrganizationCreated extends BaseDomainEvent {

    private final OrganizationId organizationId;
    private final String name;
    private final UserId createdBy;

    public OrganizationCreated(OrganizationId organizationId, String name, UserId createdBy) {
        super(organizationId.getValue(), "Organization");
        this.organizationId = organizationId;
        this.name = name;
        this.createdBy = createdBy;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }
}

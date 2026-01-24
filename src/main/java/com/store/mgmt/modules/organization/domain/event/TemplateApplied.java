package com.store.mgmt.modules.organization.domain.event;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a global template is applied to an organization.
 */
public final class TemplateApplied extends BaseDomainEvent {

    private final OrganizationId organizationId;
    private final String templateCode;

    public TemplateApplied(OrganizationId organizationId, String templateCode) {
        super(organizationId.getValue(), "Organization");
        this.organizationId = organizationId;
        this.templateCode = templateCode;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getTemplateCode() {
        return templateCode;
    }
}

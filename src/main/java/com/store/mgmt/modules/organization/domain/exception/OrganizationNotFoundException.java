package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when an organization is not found.
 */
public class OrganizationNotFoundException extends DomainException {

    public OrganizationNotFoundException(OrganizationId id) {
        super("Organization not found: " + id.getValue());
    }

    public OrganizationNotFoundException(String message) {
        super(message);
    }
}

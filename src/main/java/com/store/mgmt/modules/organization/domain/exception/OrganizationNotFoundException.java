package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.shared.domain.exception.DomainException;

import java.util.UUID;

/**
 * Exception thrown when an organization is not found.
 */
public class OrganizationNotFoundException extends DomainException {

    public OrganizationNotFoundException(UUID id) {
        super("Organization not found: " + id);
    }

    public OrganizationNotFoundException(String message) {
        super(message);
    }
}

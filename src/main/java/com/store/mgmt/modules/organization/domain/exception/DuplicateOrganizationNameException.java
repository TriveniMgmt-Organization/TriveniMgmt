package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to create an organization with a duplicate name.
 */
public class DuplicateOrganizationNameException extends DomainException {

    public DuplicateOrganizationNameException(String name) {
        super("Organization with name already exists: " + name);
    }
}

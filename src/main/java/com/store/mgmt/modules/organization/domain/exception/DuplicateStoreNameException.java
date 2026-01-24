package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to create a store with a duplicate name within an organization.
 */
public class DuplicateStoreNameException extends DomainException {

    public DuplicateStoreNameException(String name) {
        super("Store with name already exists in this organization: " + name);
    }
}

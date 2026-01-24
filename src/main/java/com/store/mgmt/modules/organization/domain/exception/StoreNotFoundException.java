package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.shared.domain.exception.DomainException;

import java.util.UUID;

/**
 * Exception thrown when a store is not found.
 */
public class StoreNotFoundException extends DomainException {

    public StoreNotFoundException(UUID id) {
        super("Store not found: " + id);
    }

    public StoreNotFoundException(String message) {
        super(message);
    }
}

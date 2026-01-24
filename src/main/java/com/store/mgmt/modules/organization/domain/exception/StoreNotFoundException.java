package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when a store is not found.
 */
public class StoreNotFoundException extends DomainException {

    public StoreNotFoundException(StoreId id) {
        super("Store not found: " + id.getValue());
    }

    public StoreNotFoundException(String message) {
        super(message);
    }
}

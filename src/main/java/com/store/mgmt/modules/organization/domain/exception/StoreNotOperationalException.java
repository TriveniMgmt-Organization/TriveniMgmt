package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.shared.domain.exception.DomainException;

import java.util.UUID;

/**
 * Exception thrown when trying to perform operations on a non-operational store.
 */
public class StoreNotOperationalException extends DomainException {

    public StoreNotOperationalException(UUID storeId, StoreStatus status) {
        super("Store " + storeId + " is not operational. Current status: " + status);
    }
}

package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when trying to perform operations on a non-operational store.
 */
public class StoreNotOperationalException extends DomainException {

    public StoreNotOperationalException(StoreId storeId, StoreStatus status) {
        super("Store " + storeId.getValue() + " is not operational. Current status: " + status);
    }
}

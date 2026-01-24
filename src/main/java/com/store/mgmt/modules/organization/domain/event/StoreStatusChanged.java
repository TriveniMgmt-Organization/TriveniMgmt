package com.store.mgmt.modules.organization.domain.event;

import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a store's status changes.
 */
public final class StoreStatusChanged extends BaseDomainEvent {

    private final StoreId storeId;
    private final StoreStatus oldStatus;
    private final StoreStatus newStatus;

    public StoreStatusChanged(StoreId storeId, StoreStatus oldStatus, StoreStatus newStatus) {
        super(storeId.getValue(), "Store");
        this.storeId = storeId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public StoreId getStoreId() {
        return storeId;
    }

    public StoreStatus getOldStatus() {
        return oldStatus;
    }

    public StoreStatus getNewStatus() {
        return newStatus;
    }
}

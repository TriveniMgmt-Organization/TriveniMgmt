package com.store.mgmt.modules.inventory.domain.event;

import com.store.mgmt.modules.inventory.domain.model.InventoryItemId;
import com.store.mgmt.modules.inventory.domain.model.StoreId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when stock level falls to or below the reorder point.
 */
public class LowStockAlertEvent extends BaseDomainEvent {

    private final StoreId storeId;
    private final int currentOnHand;
    private final int reorderPoint;

    public LowStockAlertEvent(
            InventoryItemId itemId,
            StoreId storeId,
            int currentOnHand,
            int reorderPoint
    ) {
        super(itemId.getValue(), "InventoryItem");
        this.storeId = storeId;
        this.currentOnHand = currentOnHand;
        this.reorderPoint = reorderPoint;
    }

    public StoreId getStoreId() {
        return storeId;
    }

    public int getCurrentOnHand() {
        return currentOnHand;
    }

    public int getReorderPoint() {
        return reorderPoint;
    }
}

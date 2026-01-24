package com.store.mgmt.modules.inventory.domain.exception;

import com.store.mgmt.modules.inventory.domain.model.InventoryItemId;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when there's not enough stock available.
 */
public class InsufficientStockException extends DomainException {

    private final InventoryItemId itemId;
    private final int requested;
    private final int available;

    public InsufficientStockException(InventoryItemId itemId, int requested, int available) {
        super(String.format(
                "Insufficient stock for item %s: requested %d, available %d",
                itemId.getValue(), requested, available
        ));
        this.itemId = itemId;
        this.requested = requested;
        this.available = available;
    }

    public InventoryItemId getItemId() {
        return itemId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}

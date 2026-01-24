package com.store.mgmt.modules.inventory.domain.event;

import com.store.mgmt.modules.inventory.domain.model.InventoryItemId;
import com.store.mgmt.modules.inventory.domain.model.UserId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when stock is adjusted (corrections, cycle counts, etc.).
 */
public class StockAdjustedEvent extends BaseDomainEvent {

    private final int adjustment;
    private final int previousOnHand;
    private final int newOnHand;
    private final String reason;
    private final UserId adjustedBy;

    public StockAdjustedEvent(
            InventoryItemId itemId,
            int adjustment,
            int previousOnHand,
            int newOnHand,
            String reason,
            UserId adjustedBy
    ) {
        super(itemId.getValue(), "InventoryItem");
        this.adjustment = adjustment;
        this.previousOnHand = previousOnHand;
        this.newOnHand = newOnHand;
        this.reason = reason;
        this.adjustedBy = adjustedBy;
    }

    public int getAdjustment() {
        return adjustment;
    }

    public int getPreviousOnHand() {
        return previousOnHand;
    }

    public int getNewOnHand() {
        return newOnHand;
    }

    public String getReason() {
        return reason;
    }

    public UserId getAdjustedBy() {
        return adjustedBy;
    }
}

package com.store.mgmt.modules.inventory.domain.event;

import com.store.mgmt.modules.inventory.domain.model.InventoryItemId;
import com.store.mgmt.modules.inventory.domain.model.UserId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when stock is received for an inventory item.
 */
public class StockReceivedEvent extends BaseDomainEvent {

    private final int quantity;
    private final int previousOnHand;
    private final int newOnHand;
    private final String reason;
    private final UserId receivedBy;

    public StockReceivedEvent(
            InventoryItemId itemId,
            int quantity,
            int previousOnHand,
            int newOnHand,
            String reason,
            UserId receivedBy
    ) {
        super(itemId.getValue(), "InventoryItem");
        this.quantity = quantity;
        this.previousOnHand = previousOnHand;
        this.newOnHand = newOnHand;
        this.reason = reason;
        this.receivedBy = receivedBy;
    }

    public int getQuantity() {
        return quantity;
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

    public UserId getReceivedBy() {
        return receivedBy;
    }
}

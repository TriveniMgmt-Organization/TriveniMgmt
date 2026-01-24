package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.ValueObject;

/**
 * Value object representing stock levels for an inventory item.
 * Immutable - all operations return new instances.
 */
public record StockLevel(
        int onHand,
        int reserved,
        int reorderPoint
) implements ValueObject {

    public StockLevel {
        if (onHand < 0) {
            throw new IllegalArgumentException("On hand quantity cannot be negative");
        }
        if (reserved < 0) {
            throw new IllegalArgumentException("Reserved quantity cannot be negative");
        }
        if (reserved > onHand) {
            throw new IllegalArgumentException("Reserved quantity cannot exceed on hand quantity");
        }
        if (reorderPoint < 0) {
            throw new IllegalArgumentException("Reorder point cannot be negative");
        }
    }

    /**
     * Create initial stock level.
     */
    public static StockLevel initial(int onHand, int reorderPoint) {
        return new StockLevel(onHand, 0, reorderPoint);
    }

    /**
     * Create zero stock level.
     */
    public static StockLevel zero(int reorderPoint) {
        return new StockLevel(0, 0, reorderPoint);
    }

    /**
     * Available quantity (on hand minus reserved).
     */
    public int available() {
        return onHand - reserved;
    }

    /**
     * Check if stock is at or below reorder point.
     */
    public boolean isLow() {
        return onHand <= reorderPoint;
    }

    /**
     * Check if there's enough available stock.
     */
    public boolean hasAvailable(int quantity) {
        return available() >= quantity;
    }

    /**
     * Receive stock (increase on hand).
     */
    public StockLevel receive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Receive quantity must be positive");
        }
        return new StockLevel(onHand + quantity, reserved, reorderPoint);
    }

    /**
     * Issue stock (decrease on hand).
     */
    public StockLevel issue(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Issue quantity must be positive");
        }
        if (quantity > available()) {
            throw new IllegalArgumentException("Cannot issue more than available stock");
        }
        return new StockLevel(onHand - quantity, reserved, reorderPoint);
    }

    /**
     * Adjust stock (can be positive or negative).
     */
    public StockLevel adjust(int adjustment) {
        int newOnHand = onHand + adjustment;
        if (newOnHand < 0) {
            throw new IllegalArgumentException("Adjustment would result in negative stock");
        }
        if (newOnHand < reserved) {
            throw new IllegalArgumentException("Adjustment would make reserved exceed on hand");
        }
        return new StockLevel(newOnHand, reserved, reorderPoint);
    }

    /**
     * Reserve stock for a pending order.
     */
    public StockLevel reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reserve quantity must be positive");
        }
        if (quantity > available()) {
            throw new IllegalArgumentException("Cannot reserve more than available stock");
        }
        return new StockLevel(onHand, reserved + quantity, reorderPoint);
    }

    /**
     * Release a reservation.
     */
    public StockLevel releaseReservation(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be positive");
        }
        int newReserved = Math.max(0, reserved - quantity);
        return new StockLevel(onHand, newReserved, reorderPoint);
    }

    /**
     * Commit a reservation (convert reserved to issued).
     */
    public StockLevel commitReservation(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Commit quantity must be positive");
        }
        if (quantity > reserved) {
            throw new IllegalArgumentException("Cannot commit more than reserved");
        }
        return new StockLevel(onHand - quantity, reserved - quantity, reorderPoint);
    }

    /**
     * Update the reorder point.
     */
    public StockLevel withReorderPoint(int newReorderPoint) {
        return new StockLevel(onHand, reserved, newReorderPoint);
    }
}

package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for InventoryItem.
 */
public final class InventoryItemId extends Identifier {

    private InventoryItemId(UUID value) {
        super(value);
    }

    public static InventoryItemId generate() {
        return new InventoryItemId(UUID.randomUUID());
    }

    public static InventoryItemId of(UUID value) {
        return new InventoryItemId(value);
    }

    public static InventoryItemId of(String value) {
        return new InventoryItemId(UUID.fromString(value));
    }
}

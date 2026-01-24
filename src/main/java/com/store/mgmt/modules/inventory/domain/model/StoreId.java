package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for Store.
 */
public final class StoreId extends Identifier {

    private StoreId(UUID value) {
        super(value);
    }

    public static StoreId of(UUID value) {
        return new StoreId(value);
    }

    public static StoreId of(String value) {
        return new StoreId(UUID.fromString(value));
    }
}

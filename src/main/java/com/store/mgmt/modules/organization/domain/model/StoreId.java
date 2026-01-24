package com.store.mgmt.modules.organization.domain.model;

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
        if (value == null) {
            throw new IllegalArgumentException("StoreId cannot be null");
        }
        return new StoreId(value);
    }

    public static StoreId generate() {
        return new StoreId(UUID.randomUUID());
    }
}

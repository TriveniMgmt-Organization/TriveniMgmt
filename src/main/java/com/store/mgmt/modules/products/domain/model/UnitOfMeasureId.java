package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for UnitOfMeasure.
 */
public final class UnitOfMeasureId extends Identifier {

    private UnitOfMeasureId(UUID value) {
        super(value);
    }

    public static UnitOfMeasureId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UnitOfMeasureId cannot be null");
        }
        return new UnitOfMeasureId(value);
    }

    public static UnitOfMeasureId generate() {
        return new UnitOfMeasureId(UUID.randomUUID());
    }
}

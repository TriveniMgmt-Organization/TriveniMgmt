package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for Brand.
 */
public final class BrandId extends Identifier {

    private BrandId(UUID value) {
        super(value);
    }

    public static BrandId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("BrandId cannot be null");
        }
        return new BrandId(value);
    }

    public static BrandId generate() {
        return new BrandId(UUID.randomUUID());
    }
}

package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for Category.
 */
public final class CategoryId extends Identifier {

    private CategoryId(UUID value) {
        super(value);
    }

    public static CategoryId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CategoryId cannot be null");
        }
        return new CategoryId(value);
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }
}

package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for ProductVariant.
 */
public final class ProductVariantId extends Identifier {

    private ProductVariantId(UUID value) {
        super(value);
    }

    public static ProductVariantId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("ProductVariantId cannot be null");
        }
        return new ProductVariantId(value);
    }

    public static ProductVariantId generate() {
        return new ProductVariantId(UUID.randomUUID());
    }
}

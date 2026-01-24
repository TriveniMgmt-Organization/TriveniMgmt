package com.store.mgmt.modules.inventory.domain.model;

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
        return new ProductVariantId(value);
    }

    public static ProductVariantId of(String value) {
        return new ProductVariantId(UUID.fromString(value));
    }
}

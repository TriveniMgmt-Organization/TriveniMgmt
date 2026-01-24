package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for ProductTemplate.
 */
public final class ProductTemplateId extends Identifier {

    private ProductTemplateId(UUID value) {
        super(value);
    }

    public static ProductTemplateId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("ProductTemplateId cannot be null");
        }
        return new ProductTemplateId(value);
    }

    public static ProductTemplateId generate() {
        return new ProductTemplateId(UUID.randomUUID());
    }
}

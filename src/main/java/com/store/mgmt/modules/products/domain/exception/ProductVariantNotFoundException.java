package com.store.mgmt.modules.products.domain.exception;

import com.store.mgmt.modules.products.domain.model.ProductVariantId;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when a product variant is not found.
 */
public class ProductVariantNotFoundException extends DomainException {

    public ProductVariantNotFoundException(ProductVariantId id) {
        super("Product variant not found: " + id.getValue());
    }

    public ProductVariantNotFoundException(String message) {
        super(message);
    }
}

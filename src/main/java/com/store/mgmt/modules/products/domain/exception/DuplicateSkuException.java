package com.store.mgmt.modules.products.domain.exception;

import com.store.mgmt.modules.products.domain.model.Sku;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to create a variant with a duplicate SKU.
 */
public class DuplicateSkuException extends DomainException {

    public DuplicateSkuException(Sku sku) {
        super("SKU already exists: " + sku.getValue());
    }
}

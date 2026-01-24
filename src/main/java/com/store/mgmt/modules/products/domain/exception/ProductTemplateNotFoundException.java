package com.store.mgmt.modules.products.domain.exception;

import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when a product template is not found.
 */
public class ProductTemplateNotFoundException extends DomainException {

    public ProductTemplateNotFoundException(ProductTemplateId id) {
        super("Product template not found: " + id.getValue());
    }

    public ProductTemplateNotFoundException(String message) {
        super(message);
    }
}

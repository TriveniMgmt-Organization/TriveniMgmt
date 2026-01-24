package com.store.mgmt.modules.products.domain.event;

import com.store.mgmt.modules.products.domain.model.ProductVariantId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a product variant is deactivated.
 */
public final class ProductVariantDeactivated extends BaseDomainEvent {

    private final ProductVariantId variantId;

    public ProductVariantDeactivated(ProductVariantId variantId) {
        super(variantId.getValue(), "ProductVariant");
        this.variantId = variantId;
    }

    public ProductVariantId getVariantId() {
        return variantId;
    }
}

package com.store.mgmt.modules.products.domain.event;

import com.store.mgmt.modules.products.domain.model.Money;
import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.modules.products.domain.model.ProductVariantId;
import com.store.mgmt.modules.products.domain.model.Sku;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a new product variant is created.
 */
public final class ProductVariantCreated extends BaseDomainEvent {

    private final ProductVariantId variantId;
    private final ProductTemplateId templateId;
    private final Sku sku;
    private final Money retailPrice;

    public ProductVariantCreated(
            ProductVariantId variantId,
            ProductTemplateId templateId,
            Sku sku,
            Money retailPrice
    ) {
        super(variantId.getValue(), "ProductVariant");
        this.variantId = variantId;
        this.templateId = templateId;
        this.sku = sku;
        this.retailPrice = retailPrice;
    }

    public ProductVariantId getVariantId() {
        return variantId;
    }

    public ProductTemplateId getTemplateId() {
        return templateId;
    }

    public Sku getSku() {
        return sku;
    }

    public Money getRetailPrice() {
        return retailPrice;
    }
}

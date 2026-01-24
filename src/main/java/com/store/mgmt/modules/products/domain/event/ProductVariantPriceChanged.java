package com.store.mgmt.modules.products.domain.event;

import com.store.mgmt.modules.products.domain.model.Money;
import com.store.mgmt.modules.products.domain.model.ProductVariantId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a variant's price changes.
 */
public final class ProductVariantPriceChanged extends BaseDomainEvent {

    private final ProductVariantId variantId;
    private final Money oldCostPrice;
    private final Money newCostPrice;
    private final Money oldRetailPrice;
    private final Money newRetailPrice;

    public ProductVariantPriceChanged(
            ProductVariantId variantId,
            Money oldCostPrice,
            Money newCostPrice,
            Money oldRetailPrice,
            Money newRetailPrice
    ) {
        super(variantId.getValue(), "ProductVariant");
        this.variantId = variantId;
        this.oldCostPrice = oldCostPrice;
        this.newCostPrice = newCostPrice;
        this.oldRetailPrice = oldRetailPrice;
        this.newRetailPrice = newRetailPrice;
    }

    public ProductVariantId getVariantId() {
        return variantId;
    }

    public Money getOldCostPrice() {
        return oldCostPrice;
    }

    public Money getNewCostPrice() {
        return newCostPrice;
    }

    public Money getOldRetailPrice() {
        return oldRetailPrice;
    }

    public Money getNewRetailPrice() {
        return newRetailPrice;
    }
}

package com.store.mgmt.modules.inventory.domain.event;

import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a new inventory item is created.
 */
public class InventoryItemCreatedEvent extends BaseDomainEvent {

    private final ProductVariantId variantId;
    private final LocationId locationId;
    private final StoreId storeId;
    private final int initialQuantity;
    private final UserId createdBy;

    public InventoryItemCreatedEvent(
            InventoryItemId itemId,
            ProductVariantId variantId,
            LocationId locationId,
            StoreId storeId,
            int initialQuantity,
            UserId createdBy
    ) {
        super(itemId.getValue(), "InventoryItem");
        this.variantId = variantId;
        this.locationId = locationId;
        this.storeId = storeId;
        this.initialQuantity = initialQuantity;
        this.createdBy = createdBy;
    }

    public ProductVariantId getVariantId() {
        return variantId;
    }

    public LocationId getLocationId() {
        return locationId;
    }

    public StoreId getStoreId() {
        return storeId;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }
}

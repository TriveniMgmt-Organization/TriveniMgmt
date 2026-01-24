package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for InventoryLocation.
 */
public final class LocationId extends Identifier {

    private LocationId(UUID value) {
        super(value);
    }

    public static LocationId of(UUID value) {
        return new LocationId(value);
    }

    public static LocationId of(String value) {
        return new LocationId(UUID.fromString(value));
    }
}

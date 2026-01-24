package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a single inventory item by ID.
 */
public record GetInventoryItemQuery(
        UUID itemId,
        UUID storeId
) implements Query<InventoryItemDTO> {
}

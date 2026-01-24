package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get inventory items by store with optional filters.
 */
public record GetInventoryItemsQuery(
        UUID storeId,
        Boolean lowStockOnly,
        Boolean expiringSoonOnly,
        UUID locationId,
        Integer page,
        Integer size
) implements Query<List<InventoryItemDTO>> {

    public GetInventoryItemsQuery {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (lowStockOnly == null) lowStockOnly = false;
        if (expiringSoonOnly == null) expiringSoonOnly = false;
    }
}

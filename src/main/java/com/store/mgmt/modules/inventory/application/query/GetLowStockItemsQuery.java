package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.LowStockItemResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all low stock items, optionally filtered by location.
 */
public record GetLowStockItemsQuery(UUID locationId) implements Query<List<LowStockItemResponseDTO>> {
    public GetLowStockItemsQuery() {
        this(null);
    }
}

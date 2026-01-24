package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockLevelResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get stock level for an inventory item.
 */
public record GetStockLevelQuery(UUID inventoryItemId) implements Query<StockLevelResponseDTO> {}

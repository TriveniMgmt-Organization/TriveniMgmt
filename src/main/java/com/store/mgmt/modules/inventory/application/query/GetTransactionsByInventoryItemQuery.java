package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all transactions for an inventory item.
 */
public record GetTransactionsByInventoryItemQuery(UUID inventoryItemId) implements Query<List<StockTransactionResponseDTO>> {}

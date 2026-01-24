package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a stock transaction by ID.
 */
public record GetStockTransactionByIdQuery(UUID id) implements Query<StockTransactionResponseDTO> {}

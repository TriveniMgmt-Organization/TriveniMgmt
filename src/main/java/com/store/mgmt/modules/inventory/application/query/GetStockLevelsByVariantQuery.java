package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockLevelResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all stock levels for a variant across locations.
 */
public record GetStockLevelsByVariantQuery(UUID variantId) implements Query<List<StockLevelResponseDTO>> {}

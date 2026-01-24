package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockSummaryResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get total stock summary for a specific variant.
 */
public record GetTotalStockByVariantQuery(UUID variantId) implements Query<StockSummaryResponseDTO> {}

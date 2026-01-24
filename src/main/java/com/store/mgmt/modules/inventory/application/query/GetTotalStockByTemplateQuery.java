package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockSummaryResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get total stock summary for all variants of a product template.
 */
public record GetTotalStockByTemplateQuery(UUID templateId) implements Query<List<StockSummaryResponseDTO>> {}

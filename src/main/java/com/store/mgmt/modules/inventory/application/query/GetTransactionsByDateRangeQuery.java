package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Query to get transactions within a date range.
 */
public record GetTransactionsByDateRangeQuery(
        LocalDateTime startDate,
        LocalDateTime endDate
) implements Query<List<StockTransactionResponseDTO>> {}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Query to get sales within a date range.
 */
public record GetSalesByDateRangeQuery(
        UUID storeId,
        LocalDateTime startDate,
        LocalDateTime endDate
) implements Query<List<SaleResponseDTO>> {}

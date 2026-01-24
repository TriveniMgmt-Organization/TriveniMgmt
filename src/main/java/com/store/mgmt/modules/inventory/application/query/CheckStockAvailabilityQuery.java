package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.StockAvailabilityResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to check stock availability for a variant.
 */
public record CheckStockAvailabilityQuery(
        UUID variantId,
        int requestedQuantity
) implements Query<StockAvailabilityResponseDTO> {}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a sale by ID.
 */
public record GetSaleByIdQuery(
        UUID id,
        UUID storeId
) implements Query<SaleResponseDTO> {}

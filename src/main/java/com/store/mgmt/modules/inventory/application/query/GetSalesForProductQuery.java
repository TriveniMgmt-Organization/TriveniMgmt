package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get sales for a specific product template.
 */
public record GetSalesForProductQuery(
        UUID storeId,
        UUID productTemplateId
) implements Query<List<SaleResponseDTO>> {}

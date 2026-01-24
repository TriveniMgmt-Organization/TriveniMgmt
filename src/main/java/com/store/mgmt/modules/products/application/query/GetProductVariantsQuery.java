package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get product variants with optional filters.
 */
public record GetProductVariantsQuery(
        UUID templateId,
        boolean activeOnly,
        int page,
        int size
) implements Query<List<ProductVariantDTO>> {}

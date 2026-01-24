package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a single product variant by ID.
 */
public record GetProductVariantQuery(
        UUID variantId
) implements Query<ProductVariantDTO> {}

package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.shared.application.query.Query;

/**
 * Query to search product variants by SKU or barcode.
 */
public record SearchProductVariantsQuery(
        String sku,
        String barcode
) implements Query<ProductVariantDTO> {}

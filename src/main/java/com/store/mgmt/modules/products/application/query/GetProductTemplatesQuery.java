package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get product templates with optional filters.
 */
public record GetProductTemplatesQuery(
        UUID categoryId,
        boolean activeOnly,
        int page,
        int size
) implements Query<List<ProductTemplateDTO>> {}

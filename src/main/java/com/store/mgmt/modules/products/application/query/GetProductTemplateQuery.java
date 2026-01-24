package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a single product template by ID.
 */
public record GetProductTemplateQuery(
        UUID templateId
) implements Query<ProductTemplateDTO> {}

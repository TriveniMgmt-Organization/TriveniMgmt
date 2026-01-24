package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get active discounts for a specific product.
 */
public record GetActiveDiscountsForProductQuery(UUID productTemplateId) implements Query<List<DiscountResponseDTO>> {}

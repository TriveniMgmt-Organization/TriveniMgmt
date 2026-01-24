package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get active discounts for a specific category.
 */
public record GetActiveDiscountsForCategoryQuery(UUID categoryId) implements Query<List<DiscountResponseDTO>> {}

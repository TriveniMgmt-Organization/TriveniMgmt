package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a discount by ID.
 */
public record GetDiscountByIdQuery(UUID id, UUID storeId) implements Query<DiscountResponseDTO> {}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all discounts for an organization.
 */
public record GetAllDiscountsQuery(UUID organizationId, boolean includeInactive) implements Query<List<DiscountResponseDTO>> {}

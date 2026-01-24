package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a category by ID.
 */
public record GetCategoryByIdQuery(UUID id, UUID organizationId) implements Query<CategoryResponseDTO> {}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all categories for an organization.
 */
public record GetAllCategoriesQuery(UUID organizationId, boolean includeInactive) implements Query<List<CategoryResponseDTO>> {}

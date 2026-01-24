package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a brand by ID.
 */
public record GetBrandByIdQuery(UUID id) implements Query<BrandResponseDTO> {}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.UoMConversionResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a UoM conversion by ID.
 */
public record GetUoMConversionByIdQuery(UUID id) implements Query<UoMConversionResponseDTO> {}

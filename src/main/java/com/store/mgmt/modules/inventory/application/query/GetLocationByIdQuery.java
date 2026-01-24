package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a location by ID.
 */
public record GetLocationByIdQuery(UUID id, UUID storeId) implements Query<LocationResponseDTO> {}

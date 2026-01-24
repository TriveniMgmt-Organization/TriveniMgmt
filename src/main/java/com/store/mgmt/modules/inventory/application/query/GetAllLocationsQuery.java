package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all locations for a store.
 */
public record GetAllLocationsQuery(UUID storeId, boolean includeInactive) implements Query<List<LocationResponseDTO>> {}

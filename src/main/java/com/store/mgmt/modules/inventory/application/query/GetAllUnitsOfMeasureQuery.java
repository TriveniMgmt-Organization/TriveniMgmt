package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.UnitOfMeasureResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all units of measure for an organization.
 */
public record GetAllUnitsOfMeasureQuery(UUID organizationId) implements Query<List<UnitOfMeasureResponseDTO>> {}

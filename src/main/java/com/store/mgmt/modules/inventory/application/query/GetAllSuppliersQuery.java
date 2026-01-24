package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all suppliers for an organization.
 */
public record GetAllSuppliersQuery(UUID organizationId) implements Query<List<SupplierResponseDTO>> {}

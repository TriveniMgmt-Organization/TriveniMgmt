package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a supplier by ID.
 */
public record GetSupplierByIdQuery(UUID id, UUID organizationId) implements Query<SupplierResponseDTO> {}

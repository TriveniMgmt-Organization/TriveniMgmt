package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a purchase order by ID.
 */
public record GetPurchaseOrderByIdQuery(UUID id, UUID organizationId) implements Query<PurchaseOrderResponseDTO> {}

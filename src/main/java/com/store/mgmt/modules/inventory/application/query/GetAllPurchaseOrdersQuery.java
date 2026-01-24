package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all purchase orders for an organization.
 */
public record GetAllPurchaseOrdersQuery(UUID organizationId) implements Query<List<PurchaseOrderResponseDTO>> {}

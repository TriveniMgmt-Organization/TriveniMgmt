package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get purchase orders by status.
 */
public record GetPurchaseOrdersByStatusQuery(UUID organizationId, String status) implements Query<List<PurchaseOrderResponseDTO>> {}

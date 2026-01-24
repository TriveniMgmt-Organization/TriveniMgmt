package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a batch/lot by ID.
 */
public record GetBatchLotByIdQuery(UUID id) implements Query<BatchLotResponseDTO> {}

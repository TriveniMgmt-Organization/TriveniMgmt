package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get all batch/lots.
 */
public record GetAllBatchLotsQuery(boolean includeInactive) implements Query<List<BatchLotResponseDTO>> {}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get batch/lots expiring within a specified number of days.
 */
public record GetExpiringBatchLotsQuery(int daysAhead) implements Query<List<BatchLotResponseDTO>> {
    public GetExpiringBatchLotsQuery {
        if (daysAhead < 0) {
            daysAhead = 30; // Default to 30 days
        }
    }

    public GetExpiringBatchLotsQuery() {
        this(30);
    }
}

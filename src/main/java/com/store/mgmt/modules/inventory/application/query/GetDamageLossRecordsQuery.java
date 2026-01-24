package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.DamageLossResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Query to get damage/loss records with optional filters.
 */
public record GetDamageLossRecordsQuery(
        UUID storeId,
        UUID locationId,
        LocalDate startDate,
        LocalDate endDate
) implements Query<List<DamageLossResponseDTO>> {}

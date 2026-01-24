package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.DamageLossResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a damage/loss record by ID.
 */
public record GetDamageLossByIdQuery(UUID id, UUID organizationId) implements Query<DamageLossResponseDTO> {}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.DamageLossResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to record a damage/loss.
 */
public record RecordDamageLossCommand(
        UUID organizationId,
        UUID storeId,
        UUID variantId,
        UUID locationId,
        int quantity,
        String reason,
        String notes,
        UUID userId
) implements Command<DamageLossResponseDTO> {}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.UnitOfMeasureResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to create a new unit of measure.
 */
public record CreateUnitOfMeasureCommand(
        UUID organizationId,
        String name,
        String code
) implements Command<UnitOfMeasureResponseDTO> {}

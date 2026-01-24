package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.UoMConversionResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to create a new UoM conversion.
 */
public record CreateUoMConversionCommand(
        UUID fromUomId,
        UUID toUomId,
        BigDecimal ratio
) implements Command<UoMConversionResponseDTO> {}

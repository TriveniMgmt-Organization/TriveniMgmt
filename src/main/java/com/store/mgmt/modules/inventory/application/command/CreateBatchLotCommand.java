package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to create a new batch/lot.
 */
public record CreateBatchLotCommand(
        String batchNumber,
        LocalDate manufactureDate,
        LocalDate expiryDate,
        UUID supplierId
) implements Command<BatchLotResponseDTO> {}

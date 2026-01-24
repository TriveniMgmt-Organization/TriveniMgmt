package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to create a new stock transaction.
 */
public record CreateStockTransactionCommand(
        UUID inventoryItemId,
        String type,
        int quantityDelta,
        String referenceType,
        UUID referenceId,
        String reason,
        UUID fromLocationId,
        UUID toLocationId,
        String notes,
        UUID userId
) implements Command<StockTransactionResponseDTO> {}

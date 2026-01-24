package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.shared.application.command.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to create a new inventory item.
 */
public record CreateInventoryItemCommand(
        @NotNull(message = "Variant ID is required")
        UUID variantId,

        @NotNull(message = "Location ID is required")
        UUID locationId,

        @NotNull(message = "Store ID is required")
        UUID storeId,

        String customBatchNumber,

        LocalDate expiryDate,

        @Min(value = 0, message = "Initial quantity cannot be negative")
        Integer initialQuantity,

        @Min(value = 0, message = "Low stock threshold cannot be negative")
        Integer lowStockThreshold
) implements Command<InventoryItemDTO> {

    public CreateInventoryItemCommand {
        // Default values
        if (initialQuantity == null) initialQuantity = 0;
        if (lowStockThreshold == null) lowStockThreshold = 10;
    }
}

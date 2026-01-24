package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.shared.application.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to adjust stock for an inventory item.
 */
public record AdjustStockCommand(
        @NotNull(message = "Item ID is required")
        UUID itemId,

        @NotNull(message = "Store ID is required")
        UUID storeId,

        @NotNull(message = "Adjustment is required")
        Integer adjustment,

        @NotBlank(message = "Reason is required")
        String reason
) implements Command<InventoryItemDTO> {
}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.shared.application.command.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to issue stock from an inventory item.
 */
public record IssueStockCommand(
        @NotNull(message = "Item ID is required")
        UUID itemId,

        @NotNull(message = "Store ID is required")
        UUID storeId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotBlank(message = "Reason is required")
        String reason
) implements Command<InventoryItemDTO> {
}

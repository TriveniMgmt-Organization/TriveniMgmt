package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to delete an inventory item.
 */
public record DeleteInventoryItemCommand(
        @NotNull(message = "Item ID is required")
        UUID itemId,

        @NotNull(message = "Store ID is required")
        UUID storeId
) implements Command<Void> {
}

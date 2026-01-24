package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to deactivate a discount.
 */
public record DeactivateDiscountCommand(UUID id, UUID storeId) implements Command<Void> {}

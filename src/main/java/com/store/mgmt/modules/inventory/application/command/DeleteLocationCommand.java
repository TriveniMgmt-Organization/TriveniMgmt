package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to soft delete an inventory location.
 */
public record DeleteLocationCommand(UUID id, UUID storeId) implements Command<Void> {}

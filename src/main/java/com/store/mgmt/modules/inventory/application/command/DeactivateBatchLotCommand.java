package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to deactivate a batch/lot.
 */
public record DeactivateBatchLotCommand(UUID id) implements Command<Void> {}

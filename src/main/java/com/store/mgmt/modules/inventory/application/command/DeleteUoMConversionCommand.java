package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to delete a UoM conversion.
 */
public record DeleteUoMConversionCommand(UUID id) implements Command<Void> {}

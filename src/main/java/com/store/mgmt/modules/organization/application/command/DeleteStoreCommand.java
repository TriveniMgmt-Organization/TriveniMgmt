package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to delete a store.
 */
public record DeleteStoreCommand(
        UUID storeId
) implements Command<Void> {}

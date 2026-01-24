package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to soft delete a supplier.
 */
public record DeleteSupplierCommand(UUID id, UUID organizationId) implements Command<Void> {}

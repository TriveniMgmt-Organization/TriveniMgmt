package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to soft delete a category.
 */
public record DeleteCategoryCommand(UUID id, UUID organizationId) implements Command<Void> {}

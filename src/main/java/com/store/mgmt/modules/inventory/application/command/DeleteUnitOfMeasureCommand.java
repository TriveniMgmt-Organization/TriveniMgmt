package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to soft delete a unit of measure.
 */
public record DeleteUnitOfMeasureCommand(UUID id, UUID organizationId) implements Command<Void> {}

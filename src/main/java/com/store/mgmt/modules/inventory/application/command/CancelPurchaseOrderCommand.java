package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to cancel a purchase order.
 */
public record CancelPurchaseOrderCommand(UUID id, UUID organizationId) implements Command<Void> {}

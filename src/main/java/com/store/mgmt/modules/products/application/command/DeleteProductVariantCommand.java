package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to delete (soft delete) a product variant.
 */
public record DeleteProductVariantCommand(
        UUID variantId
) implements Command<Void> {}

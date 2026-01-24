package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for creating a new stock transaction.
 */
public record CreateStockTransactionRequestDTO(
        @NotNull(message = "Inventory item ID is required")
        UUID inventoryItemId,

        @NotNull(message = "Transaction type is required")
        String type,

        @NotNull(message = "Quantity delta is required")
        Integer quantityDelta,

        String referenceType,

        UUID referenceId,

        String reason,

        UUID fromLocationId,

        UUID toLocationId,

        String notes
) {}

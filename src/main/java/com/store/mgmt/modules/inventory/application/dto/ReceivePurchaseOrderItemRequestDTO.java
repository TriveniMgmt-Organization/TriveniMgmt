package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for receiving a purchase order item.
 */
public record ReceivePurchaseOrderItemRequestDTO(
        @NotNull(message = "PO Item ID is required")
        UUID poItemId,

        @NotNull(message = "Received quantity is required")
        @Min(value = 1, message = "Received quantity must be at least 1")
        Integer receivedQuantity,

        @NotNull(message = "Location ID is required")
        UUID locationId,

        String batchNumber,

        LocalDate expiryDate
) {}

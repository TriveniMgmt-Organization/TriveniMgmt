package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for creating a new damage/loss record.
 */
public record CreateDamageLossRequestDTO(
        @NotNull(message = "Variant ID is required")
        UUID variantId,

        @NotNull(message = "Location ID is required")
        UUID locationId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @NotNull(message = "Reason is required")
        String reason,

        String notes
) {}

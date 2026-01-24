package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a sale item.
 */
public record CreateSaleItemRequestDTO(
        @NotNull(message = "Product template ID is required")
        UUID productTemplateId,

        @NotNull(message = "Variant ID is required")
        UUID variantId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        BigDecimal unitPrice,

        BigDecimal discountAmount
) {}

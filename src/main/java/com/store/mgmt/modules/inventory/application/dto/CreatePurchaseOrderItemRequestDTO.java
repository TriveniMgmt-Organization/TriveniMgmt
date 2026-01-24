package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a purchase order item.
 */
public record CreatePurchaseOrderItemRequestDTO(
        @NotNull(message = "Store ID is required")
        UUID storeId,

        @NotNull(message = "Product template ID is required")
        UUID productTemplateId,

        @NotNull(message = "Variant ID is required")
        UUID variantId,

        @NotNull(message = "Ordered quantity is required")
        @Min(value = 1, message = "Ordered quantity must be at least 1")
        Integer orderedQuantity,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.01", message = "Unit cost must be at least 0.01")
        BigDecimal unitCost
) {}

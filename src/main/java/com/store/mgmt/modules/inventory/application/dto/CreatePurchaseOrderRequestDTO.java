package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new purchase order.
 */
public record CreatePurchaseOrderRequestDTO(
        @NotNull(message = "Supplier ID is required")
        UUID supplierId,

        LocalDate expectedDeliveryDate,

        String trackingNumber,

        String notes,

        @NotEmpty(message = "At least one item is required")
        @Valid
        List<CreatePurchaseOrderItemRequestDTO> items
) {}

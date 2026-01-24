package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a sale.
 */
public record CreateSaleRequestDTO(
        @NotNull(message = "Payment method is required")
        String paymentMethod,

        String transactionId,

        String notes,

        @NotEmpty(message = "Sale items are required")
        @Valid
        List<CreateSaleItemRequestDTO> items
) {}

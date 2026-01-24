package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for receiving a purchase order.
 */
public record ReceivePurchaseOrderRequestDTO(
        @NotEmpty(message = "At least one item to receive is required")
        @Valid
        List<ReceivePurchaseOrderItemRequestDTO> items
) {}

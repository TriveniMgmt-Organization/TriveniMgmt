package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDate;

/**
 * DTO for updating a purchase order.
 */
public record UpdatePurchaseOrderRequestDTO(
        LocalDate expectedDeliveryDate,
        String trackingNumber,
        String notes,
        String status
) {}

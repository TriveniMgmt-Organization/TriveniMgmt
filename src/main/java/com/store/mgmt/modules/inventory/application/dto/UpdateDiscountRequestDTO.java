package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for updating a discount.
 */
public record UpdateDiscountRequestDTO(
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,

        String type,

        @DecimalMin(value = "0.0001", message = "Value must be greater than 0")
        BigDecimal value,

        LocalDate startDate,

        LocalDate endDate,

        UUID productTemplateId,

        UUID categoryId,

        String description,

        BigDecimal minimumPurchaseAmount,

        Integer minimumItemQuantity,

        Boolean isActive
) {}

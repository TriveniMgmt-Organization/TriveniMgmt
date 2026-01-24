package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new discount.
 */
public record CreateDiscountRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,

        @NotNull(message = "Discount type is required")
        String type,

        @NotNull(message = "Value is required")
        @DecimalMin(value = "0.0001", message = "Value must be greater than 0")
        BigDecimal value,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        UUID productTemplateId,

        UUID categoryId,

        String description,

        BigDecimal minimumPurchaseAmount,

        Integer minimumItemQuantity,

        Boolean isActive
) {
    public CreateDiscountRequestDTO {
        if (isActive == null) {
            isActive = true;
        }
    }
}

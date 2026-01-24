package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new UoM conversion.
 */
public record CreateUoMConversionRequestDTO(
        @NotNull(message = "From UoM ID is required")
        UUID fromUomId,

        @NotNull(message = "To UoM ID is required")
        UUID toUomId,

        @NotNull(message = "Conversion ratio is required")
        @DecimalMin(value = "0.0001", message = "Ratio must be greater than 0")
        BigDecimal ratio
) {}

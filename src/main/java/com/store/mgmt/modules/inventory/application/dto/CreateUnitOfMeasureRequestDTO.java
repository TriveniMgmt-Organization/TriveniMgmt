package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a unit of measure.
 */
public record CreateUnitOfMeasureRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @NotBlank(message = "Code is required")
        @Size(min = 1, max = 20, message = "Code must be between 1 and 20 characters")
        String code
) {}

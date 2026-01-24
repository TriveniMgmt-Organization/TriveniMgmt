package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating a unit of measure.
 */
public record UpdateUnitOfMeasureRequestDTO(
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @Size(min = 1, max = 20, message = "Code must be between 1 and 20 characters")
        String code
) {}

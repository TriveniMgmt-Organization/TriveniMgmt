package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a category.
 */
public record CreateCategoryRequestDTO(
        @NotBlank(message = "Code is required")
        @Size(min = 2, max = 50, message = "Code must be between 2 and 50 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        Boolean isActive
) {
    public CreateCategoryRequestDTO {
        if (isActive == null) {
            isActive = true;
        }
    }
}

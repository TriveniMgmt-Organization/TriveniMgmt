package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating a category.
 */
public record UpdateCategoryRequestDTO(
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        Boolean isActive
) {}

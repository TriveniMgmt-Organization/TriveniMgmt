package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a brand.
 */
public record CreateBrandRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
        String logoUrl,

        @Size(max = 255, message = "Website cannot exceed 255 characters")
        String website,

        Boolean isActive
) {
    public CreateBrandRequestDTO {
        if (isActive == null) {
            isActive = true;
        }
    }
}

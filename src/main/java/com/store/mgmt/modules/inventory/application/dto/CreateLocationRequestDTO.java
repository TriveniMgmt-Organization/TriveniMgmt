package com.store.mgmt.modules.inventory.application.dto;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating an inventory location.
 */
public record CreateLocationRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        String name,

        String address,

        @NotNull(message = "Type is required")
        InventoryLocationType type,

        Boolean isActive
) {
    public CreateLocationRequestDTO {
        if (isActive == null) {
            isActive = true;
        }
    }
}

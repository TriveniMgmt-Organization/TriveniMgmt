package com.store.mgmt.modules.inventory.application.dto;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocationType;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an inventory location.
 */
public record UpdateLocationRequestDTO(
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        String name,

        String address,

        InventoryLocationType type,

        Boolean isActive
) {}

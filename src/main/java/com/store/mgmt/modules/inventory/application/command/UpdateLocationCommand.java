package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocationType;
import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update an existing inventory location.
 */
public record UpdateLocationCommand(
        UUID id,
        UUID storeId,
        String name,
        String address,
        InventoryLocationType type,
        Boolean isActive
) implements Command<LocationResponseDTO> {}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update an existing brand.
 */
public record UpdateBrandCommand(
        UUID id,
        String name,
        String description,
        String logoUrl,
        String website,
        Boolean isActive
) implements Command<BrandResponseDTO> {}

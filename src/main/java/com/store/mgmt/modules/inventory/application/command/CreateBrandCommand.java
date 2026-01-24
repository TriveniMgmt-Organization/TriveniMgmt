package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import com.store.mgmt.shared.application.command.Command;

/**
 * Command to create a new brand.
 */
public record CreateBrandCommand(
        String name,
        String description,
        String logoUrl,
        String website,
        Boolean isActive
) implements Command<BrandResponseDTO> {}

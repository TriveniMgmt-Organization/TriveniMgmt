package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update an existing category.
 */
public record UpdateCategoryCommand(
        UUID id,
        UUID organizationId,
        String name,
        String description,
        Boolean isActive
) implements Command<CategoryResponseDTO> {}

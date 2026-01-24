package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to create a new category.
 */
public record CreateCategoryCommand(
        UUID organizationId,
        String code,
        String name,
        String description,
        Boolean isActive
) implements Command<CategoryResponseDTO> {}

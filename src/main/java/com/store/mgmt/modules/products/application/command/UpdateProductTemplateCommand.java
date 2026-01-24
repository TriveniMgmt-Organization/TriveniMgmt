package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.Map;
import java.util.UUID;

/**
 * Command to update an existing product template.
 */
public record UpdateProductTemplateCommand(
        UUID templateId,
        String name,
        String description,
        UUID categoryId,
        UUID unitOfMeasureId,
        UUID brandId,
        String imageUrl,
        Integer reorderPoint,
        Boolean requiresExpiry,
        Map<String, String> attributes
) implements Command<ProductTemplateDTO> {}

package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.Map;
import java.util.UUID;

/**
 * Command to create a new product template.
 */
public record CreateProductTemplateCommand(
        String name,
        String description,
        UUID categoryId,
        UUID unitOfMeasureId,
        UUID brandId,
        String imageUrl,
        Integer reorderPoint,
        boolean requiresExpiry,
        Map<String, String> attributes
) implements Command<ProductTemplateDTO> {}

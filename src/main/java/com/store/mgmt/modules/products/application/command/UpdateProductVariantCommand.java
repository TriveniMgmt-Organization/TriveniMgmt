package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.shared.application.command.Command;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Command to update an existing product variant.
 */
public record UpdateProductVariantCommand(
        UUID variantId,
        String sku,
        String barcode,
        BigDecimal costPrice,
        BigDecimal retailPrice,
        Map<String, String> attributeValues,
        Boolean active
) implements Command<ProductVariantDTO> {}

package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ProductVariant", description = "Data Transfer Object for a Product Variant (contains SKU and barcode)")
public class ProductVariantDTO {

    @Schema(
            description = "Unique identifier of the variant",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(description = "Unique identifier of the product template", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID templateId;

    @Schema(description = "Product template details", nullable = true)
    private ProductTemplateDTO template;

    @Schema(
            description = "SKU (Stock Keeping Unit) - unique within organization",
            example = "LAPTOP-X1-256GB",
            minLength = 2,
            maxLength = 50
    )
    private String sku;

    @Schema(
            description = "Unique barcode identifier - unique within organization",
            example = "1234567890123",
            pattern = "^[0-9]{12,13}$"
    )
    private String barcode;

    @Schema(description = "Cost price of the variant", minimum = "0.01", maximum = "1000000.00")
    private BigDecimal costPrice;

    @Schema(description = "Retail price of the variant", minimum = "0.01", maximum = "1000000.00")
    private BigDecimal retailPrice;

    @Schema(description = "Variant-specific attribute values")
    private Map<String, String> attributeValues;

    @Schema(description = "Whether this variant is active")
    private boolean isActive;
}


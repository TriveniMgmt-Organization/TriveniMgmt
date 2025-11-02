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
@Schema(name = "CreateProductVariant", description = "Data Transfer Object for creating a Product Variant (SKU/barcode at variant level)")
public class CreateProductVariantDTO {

    @Schema(
            description = "Unique identifier of the product template this variant belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID templateId;

    @Schema(
            description = "SKU (Stock Keeping Unit) - unique within organization",
            example = "LAPTOP-X1-256GB",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 50
    )
    private String sku;

    @Schema(
            description = "Unique barcode identifier - unique within organization",
            example = "1234567890123",
            pattern = "^[0-9]{12,13}$" // EAN-13 or UPC-A
    )
    private String barcode;

    @Schema(
            description = "Cost price of the variant",
            example = "999.99",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01",
            maximum = "1000000.00"
    )
    private BigDecimal costPrice;

    @Schema(
            description = "Retail price of the variant",
            example = "1299.99",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01",
            maximum = "1000000.00"
    )
    private BigDecimal retailPrice;

    @Schema(description = "Variant-specific attribute values (e.g., {\"color\": \"Red\", \"size\": \"L\"})")
    private Map<String, String> attributeValues;

    @Schema(description = "Whether this variant is active")
    private boolean isActive = true;
}


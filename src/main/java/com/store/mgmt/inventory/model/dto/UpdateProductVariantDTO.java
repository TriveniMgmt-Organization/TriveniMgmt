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
@Schema(name = "UpdateProductVariant", description = "Data Transfer Object for updating a Product Variant")
public class UpdateProductVariantDTO {

    @Schema(description = "SKU (Stock Keeping Unit)", example = "LAPTOP-X1-256GB")
    private String sku;

    @Schema(description = "Unique barcode identifier", example = "1234567890123")
    private String barcode;

    @Schema(description = "Cost price of the variant", minimum = "0.01", maximum = "1000000.00")
    private BigDecimal costPrice;

    @Schema(description = "Retail price of the variant", minimum = "0.01", maximum = "1000000.00")
    private BigDecimal retailPrice;

    @Schema(description = "Variant-specific attribute values")
    private Map<String, String> attributeValues;

    @Schema(description = "Whether this variant is active")
    private Boolean isActive;
}


package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ProductTemplate", description = "Data Transfer Object for a Product Template (master product definition without SKU/barcode)")
public class ProductTemplateDTO {

    @Schema(
            description = "Unique identifier of the product template",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Name of the product template",
            example = "Laptop Pro X",
            minLength = 2,
            maxLength = 100
    )
    private String name;

    @Schema(description = "Detailed description of the product")
    private String description;

    @Schema(
            description = "Unique identifier of the category this product template belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210"
    )
    private UUID categoryId;

    @Schema(description = "Details of the product template's category", nullable = true)
    private CategoryDTO category;

    @Schema(description = "Unique identifier of the brand this product template belongs to")
    private UUID brandId;

    @Schema(description = "Details of the brand", nullable = true)
    private BrandDTO brand;

    @Schema(
            description = "Unique identifier of the Unit of Measure this product template uses",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID unitOfMeasureId;

    @Schema(description = "Details of the unit of measure", nullable = true)
    private UnitOfMeasureDTO unitOfMeasure;

    @Schema(description = "Image URL of this product template", accessMode = Schema.AccessMode.READ_ONLY)
    private String imageUrl;

    @Schema(description = "Reorder point for inventory management")
    private Integer reorderPoint;

    @Schema(description = "Whether this product requires expiry date tracking")
    private boolean requiresExpiry;

    @Schema(description = "Dynamic attributes schema (e.g., color, size) - defines what attributes variants can have")
    private Map<String, String> attributes;

    @Schema(description = "List of product variants associated with this template")
    private java.util.List<ProductVariantDTO> variants;
}


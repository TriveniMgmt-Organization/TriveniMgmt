package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdateProduct", description = "Data Transfer Object for updating a Product Template")
public class UpdateProductDTO {

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

    @Schema(
            description = "Detailed description of the product",
            example = "High-performance laptop with 16GB RAM and 512GB SSD.",
            minLength = 10,
            maxLength = 500
    )
    private String description;

    @Schema(description = "Unique identifier of the category")
    private UUID categoryId;

    @Schema(description = "Unique identifier of the brand")
    private UUID brandId;

    @Schema(description = "Unique identifier of the Unit of Measure")
    private UUID unitOfMeasureId;

    @Schema(description = "Image URL of this product")
    private String imageUrl;

    @Schema(description = "Reorder point for inventory management")
    private Integer reorderPoint;

    @Schema(description = "Whether this product requires expiry date tracking")
    private Boolean requiresExpiry;
}
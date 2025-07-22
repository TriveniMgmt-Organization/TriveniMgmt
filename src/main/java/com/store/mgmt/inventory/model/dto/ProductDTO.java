package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "Product", description = "Data Transfer Object for a product item available for sale")
public class ProductDTO {

    @Schema(
            description = "Unique identifier of the product",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Name of the product",
            example = "Laptop Pro X",
            minLength = 2,
            maxLength = 100
    )
    private String name;

    @Schema(
            description = "SKU (Stock Keeping Unit) of the product",
            example = "LPX-2023-001",
            minLength = 2,
            maxLength = 50
    )
    private String sku;

    @Schema( description = "Detailed description of the product" )
    private String description;

    @Schema( description = "Selling price of the product" )
    private BigDecimal price;

    @Schema( description = "Current stock quantity of the product", minimum = "0", maximum = "1000000")
    private Integer quantity;

    @Schema(
            description = "Unique barcode identifier for the product",
            example = "1234567890123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "^[0-9]{12,13}$" // Example for EAN-13 or UPC-A
    )
    private String barcode;

    @Schema(
            description = "Unique identifier of the category this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID categoryId;

    @Schema(
            description = "Details of the product's category",
            nullable = true // Category might not always be fetched/included
    )
    private CategoryDTO category; // Embedded Category DTO

    @Schema(
            description = "Image Url of this product",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String imageUrl;
}
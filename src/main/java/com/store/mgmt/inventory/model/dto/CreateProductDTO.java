package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "CreateProduct", description = "Data Transfer Object for a product item available for sale")
public class CreateProductDTO {

    @Schema(
            description = "Name of the product",
            example = "Laptop Pro X",
            required = true,
            minLength = 2,
            maxLength = 20
    )
    private String sku;

    @Schema(
            description = "Name of the product",
            example = "Laptop Pro X",
            required = true,
            minLength = 2,
            maxLength = 100
    )
    private String name;

    @Schema(
            description = "Selling price of the product",
            example = "1200.50",
            required = true,
            minimum = "0.01" // Price must be greater than zero
    )
    private BigDecimal price;

    @Schema(
            description = "Current stock quantity of the product",
            example = "50",
            required = true,
            minimum = "0" // Quantity can be zero if out of stock
    )
    private Integer quantity;

    @Schema(
            description = "Unique barcode identifier for the product",
            example = "1234567890123",
            required = true,
            pattern = "^[0-9]{12,13}$" // Example for EAN-13 or UPC-A
    )
    private String barcode;

    @Schema(
            description = "Unique identifier of the category this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID categoryId;

    @Schema(
            description = "Unique identifier of the Unit measure of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID unitOfMeasureId;
}
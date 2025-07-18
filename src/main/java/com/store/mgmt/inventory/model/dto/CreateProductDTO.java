package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "create_product", description = "Data Transfer Object for a product item available for sale")
public class CreateProductDTO {

    @Schema(
            description = "Name of the product",
            example = "Laptop Pro X",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 20
    )
    private String sku;

    @Schema(
            description = "Name of the product",
            example = "Laptop Pro X",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 100
    )
    private String name;

    @Schema(
            description = "Detailed description of the product",
            example = "High-performance laptop with 16GB RAM and 512GB SSD.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            minLength = 10,
            maxLength = 500
    )
    private String description;

    @Schema(
            description = "Selling price of the product",
            example = "1200.50",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01", // Price must be greater than zero
            maximum = "1000000.00" // Reasonable upper limit for product price
    )
    private BigDecimal price;

    @Schema(
            description = "Current stock quantity of the product",
            example = "50",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0" ,// Quantity can be zero if out of stock
            maximum = "1000000"
    )
    private Integer quantity;

    @Schema(
            description = "Unique barcode identifier for the product",
            example = "1234567890123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "^[0-9]{12,13}$" // Example for EAN-13 or UPC-A
    )
    private String barcode;

    @Schema(
            name="category_id",
            description = "Unique identifier of the category this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID categoryId;

    @Schema(
            name="unit_of_measure",
            description = "Unique identifier of the Unit measure of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID unitOfMeasureId;

    @Schema(
            name="image_url",
            description = "Image Url of this product"
    )
    private String imageUrl;

    @Schema(
            name="image_file",
            description = "Image file of the product",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private MultipartFile imageFile;
}
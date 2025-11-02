package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "CreateProduct", description = "Data Transfer Object for creating a Product Template (master product definition)")
public class CreateProductDTO {

    @Schema(
            description = "Name of the product template",
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
            description = "Unique identifier of the category this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID categoryId;

    @Schema(
            description = "Unique identifier of the Unit measure of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID unitOfMeasureId;

    @Schema(
            description = "Unique identifier of the brand (optional)",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private UUID brandId;

    @Schema(
            description = "Image Url of this product"
    )
    private String imageUrl;

    @Schema(
            description = "Image file of the product",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private MultipartFile imageFile;
}
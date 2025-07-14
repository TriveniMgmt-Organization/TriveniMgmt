package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "Category", description = "Data Transfer Object for a product category")
public class CategoryDTO {

    @Schema(
            description = "Unique identifier of the category",
            example = "fedcba98-7654-3210-fedc-ba9876543210"
    )
    private UUID id;

    @Schema(
            description = "Code of the category",
            example = "Electronics",
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = 2,
            maxLength = 5
    )
    private String code;

    @Schema(
            description = "Name of the category",
            example = "Electronics",
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = 2,
            maxLength = 50
    )
    private String name;

    @Schema(
            description = "Description of the category",
            example = "Devices and gadgets.",
            nullable = true,
            maxLength = 255
    )
    private String description;
}
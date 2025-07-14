package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "CreateCategory", description = "Data Transfer Object for a category item available for sale")
public class CreateCategoryDTO {
    @Schema(
            description = "Code of the category",
            example = "LPXT",
            required = true,
            minLength = 2,
            maxLength = 5
    )
    private String code;

    @Schema(
            description = "Name of the category",
            example = "Laptop Pro X",
            required = true,
            minLength = 2,
            maxLength = 100
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
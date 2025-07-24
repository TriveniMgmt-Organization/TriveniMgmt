package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CreateBrand", description = "Data Transfer Object for a category item available for sale")
public class CreateBrandDTO {
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

    @Schema(
            description = "URL of the brand logo",
            example = "https://example.com/logo.png",
            nullable = true,
            maxLength = 255
    )
    private String logoUrl;

    @Schema(
            description = "Website of the brand",
            example = "https://example.com",
            nullable = true,
            maxLength = 255
    )
    private String website;

}
package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Location", description = "Data Transfer Object for a product category")
public class LocationDTO {
    @Schema(description = "Unique identifier for the location")
    private String id;

    @Schema(description = "Name of the location")
    private String name;

    @Schema(description = "Description of the location")
    private String description;

    @Schema(description = "Address of the location")
    private String address;

    @Schema(description = "Contact number for the location")
    private String contactNumber;
}
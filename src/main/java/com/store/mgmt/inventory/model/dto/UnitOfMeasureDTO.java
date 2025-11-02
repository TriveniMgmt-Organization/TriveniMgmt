package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UnitOfMeasure", description = "Data Transfer Object for a product category")
public class UnitOfMeasureDTO {
    @Schema(description = "Unique identifier for the unit of measure")
    private UUID id;

    @Schema(description = "Unique code for the unit of measure")
    private String code;

    @Schema(description = "Name of the unit of measure")
    private String name;

    @Schema(description = "Description of the unit of measure")
    private String description;

    @Schema(description = "Conversion factor to the base unit")
    private double conversionFactor;
}
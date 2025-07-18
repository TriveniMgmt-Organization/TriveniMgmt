package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "unit_of_measure", description = "Data Transfer Object for a product category")
public class UnitOfMeasureDTO {
    @Schema(description = "Unique identifier for the unit of measure")
    private UUID id;

    @Schema(description = "Name of the unit of measure")
    private String name;

    @Schema(description = "Description of the unit of measure")
    private String description;

    @Schema(name="conversion_factor", description = "Conversion factor to the base unit")
    private double conversionFactor;
}
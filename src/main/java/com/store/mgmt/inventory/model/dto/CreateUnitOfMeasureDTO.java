package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "create_unit_of_Measure", description = "Data Transfer Object for a product category")
public class CreateUnitOfMeasureDTO {
    @Schema(description = "Name of the unit of measure", required = true)
    private String name;

    @Schema(description = "Unique code for the unit of measure", required = true)
    private String code;

    @Schema(description = "Description of the unit of measure")
    private String description;


    @Schema(name="conversion_factor", description = "Conversion factor for the unit of measure")
    private double conversionFactor;

    @Schema(name="base_unit", description = "Base unit of measure for conversion")
    private String baseUnit;
}
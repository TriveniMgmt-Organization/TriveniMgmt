package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdateUnitOfMeasure", description = "Data Transfer Object for a product category")
public class UpdateUnitOfMeasureDTO extends CreateUnitOfMeasureDTO {
    @Schema(description = "Unique identifier of the unit of measure to be updated", required = true)
    private UUID id;
}
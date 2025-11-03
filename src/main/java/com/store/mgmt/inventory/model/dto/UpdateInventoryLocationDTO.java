package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UpdateInventoryLocation", description = "Data Transfer Object for a product category")
public class UpdateInventoryLocationDTO extends CreateInventoryLocationDTO {
    @Schema(description = "Unique identifier for the location", required = true)
    private UUID id;
}
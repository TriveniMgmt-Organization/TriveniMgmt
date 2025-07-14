package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdateInventoryItem", description = "Data Transfer Object for a product category")
public class UpdateInventoryItemDTO extends CreateInventoryItemDTO {
    @Schema(description = "Unique identifier for the inventory item", required = true)
    private UUID id;

    @Schema(description = "Quantity changed in the inventory item", required = true)
    private Integer quantityChange;
}
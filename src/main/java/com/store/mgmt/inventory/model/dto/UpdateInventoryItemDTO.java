package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "update_inventory_item", description = "Data Transfer Object for a product category")
public class UpdateInventoryItemDTO extends CreateInventoryItemDTO {
    @Schema(description = "Unique identifier for the inventory item", required = true)
    private UUID id;

    @Schema(name="quantity_change", description = "Quantity changed in the inventory item", required = true, maximum = "1000000", minimum = "0")
    private Integer quantityChange;
}
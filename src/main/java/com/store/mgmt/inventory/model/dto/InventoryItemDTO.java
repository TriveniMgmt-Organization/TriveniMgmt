package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "inventory_item", description = "Data Transfer Object for a product category")
public class InventoryItemDTO {
    @Schema(description = "Unique identifier for the inventory item")
    private String id;

    @Schema(description = "Name of the inventory item")
    private String name;

    @Schema(description = "Description of the inventory item")
    private String description;

    @Schema(description = "Quantity of the inventory item", minimum = "0", maximum = "1000000",  example = "100")
    private Integer quantity;

    @Schema(name="unit_of_measure", description = "Unit of measure for the inventory item")
    private String unitOfMeasure;

    @Schema(description = "Location where the inventory item is stored")
    private LocationDTO location;
}
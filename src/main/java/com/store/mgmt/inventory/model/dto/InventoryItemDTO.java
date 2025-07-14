package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "InventoryItem", description = "Data Transfer Object for a product category")
public class InventoryItemDTO {
    @Schema(description = "Unique identifier for the inventory item")
    private String id;

    @Schema(description = "Name of the inventory item")
    private String name;

    @Schema(description = "Description of the inventory item")
    private String description;

    @Schema(description = "Quantity of the inventory item")
    private int quantity;

    @Schema(description = "Unit of measure for the inventory item")
    private String unitOfMeasure;

    @Schema(description = "Location where the inventory item is stored")
    private String location;
}
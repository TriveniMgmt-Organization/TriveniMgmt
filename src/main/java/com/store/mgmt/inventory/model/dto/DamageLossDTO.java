package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "damage_loss", description = "Data Transfer Object for a product category")
public class DamageLossDTO {
    @Schema(description = "Unique identifier for the damage or loss record", required = true)
    private String id;

    @Schema(name="inventory_item_id",description = "Unique identifier for the inventory item", required = true)
    private String inventoryItemId;

    @Schema(description = "Quantity of the item that is damaged or lost", required = true, minimum = "0", maximum = "1000000")
    private Integer quantity;

    @Schema(description = "Description of the damage or loss")
    private String description;

    @Schema(description = "Date when the damage or loss occurred", required = true)
    private String date;
}
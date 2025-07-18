package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "create_purchase_order_item", description = "Data Transfer Object for a product category")
public class CreatePurchaseOrderItemDTO {
    @Schema(description = "Name of the purchase order item", required = true)
    private String name;

    @Schema(description = "Description of the purchase order item")
    private String description;

    @Schema(description = "Quantity of the purchase order item", required = true, minimum = "0", maximum = "1000000", example = "10")
    private Integer quantity;

    @Schema(name="unit_of_measure",description = "Unit of measure for the purchase order item", required = true)
    private String unitOfMeasure;

    @Schema(description = "Location where the purchase order item is stored")
    private String location;
}
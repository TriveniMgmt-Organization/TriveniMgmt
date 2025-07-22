package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "SaleItem", description = "Data Transfer Object for a product category")
public class SaleItemDTO {
    @Schema(description = "Unique identifier for the sale item")
    private UUID id;

    @Schema( description = "Unique identifier for the sale")
    private String saleId;

    @Schema( description = "Unique identifier for the inventory item")
    private String inventoryItemId;

    @Schema(description = "Quantity of the inventory item sold", minimum = "0", maximum = "1000000" )
    private Integer quantity;

    @Schema(description = "Price of the inventory item at the time of sale")
    private Double price;

    @Schema(
            description = "Unique identifier of the Product of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID productTemplateId;
}
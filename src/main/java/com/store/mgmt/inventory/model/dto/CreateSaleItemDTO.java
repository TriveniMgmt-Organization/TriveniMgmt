package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(name = "CreateSaleItem", description = "Data Transfer Object for a product category")
public class CreateSaleItemDTO {
    @Schema(description = "Name of the sale item", required = true)
    private String name;

    @Schema(description = "Description of the sale item")
    private String description;

    @Schema(description = "Price of the sale item", required = true)
    private double price;

    @Schema(description = "Quantity of the sale item", required = true)
    private int quantity;

    @Schema(description = "Unit of measure for the sale item")
    private String unitOfMeasure;

    @Schema(description = "Unique identifier for the sale this item belongs to", required = true)
    private BigDecimal unitPrice; // Price at the time of sale (historical)

    @Schema(description = "Discount amount applied to the sale item", required = true)
    private BigDecimal discountAmount;

    @Schema(
            description = "Unique identifier of the Product of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID productId;
}
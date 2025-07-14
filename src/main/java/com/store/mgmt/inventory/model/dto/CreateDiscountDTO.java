package com.store.mgmt.inventory.model.dto;

import com.store.mgmt.inventory.model.entity.Discount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "CreateDiscount", description = "Data Transfer Object for a product category")
public class CreateDiscountDTO {
    @Schema(description = "Name of the discount", required = true)
    private String name;

    @Schema(description = "Description of the discount")
    private String description;

    @Schema(description = "Discount percentage", required = true)
    private double discountAmount;

    @Schema(description = "Type of the discount", required = true)
    private Discount.DiscountType type;
    @Schema(description = "Unique identifier of the product to which the discount applies", required = true)
    private UUID productId;

    @Schema(description = "Unique identifier of the category to which the discount applies", required = true)
    private UUID categoryId;
}
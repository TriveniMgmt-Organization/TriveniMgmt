package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdateDiscount", description = "Data Transfer Object for a product category")
public class UpdateDiscountDTO extends CreateDiscountDTO {
    @Schema(description = "Unique identifier for the discount", required = true)
    private UUID id;
}
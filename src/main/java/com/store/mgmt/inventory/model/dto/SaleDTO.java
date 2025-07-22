package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "Sale", description = "Data Transfer Object for a product category")
public class SaleDTO {
    @Schema(description = "Unique identifier for the sale", required = true)
    private UUID id;

    @Schema(description = "Name of the sale", required = true)
    private String name;

    @Schema(description = "Description of the sale")
    private String description;

    @Schema(description = "Discount percentage for the sale", required = true)
    private double discountPercentage;

    @Schema(description = "Start date of the sale", required = true)
    private String startDate;

    @Schema(description = "End date of the sale", required = true)
    private String endDate;
}
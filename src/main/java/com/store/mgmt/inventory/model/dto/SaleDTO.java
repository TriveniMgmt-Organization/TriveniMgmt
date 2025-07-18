package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "sale", description = "Data Transfer Object for a product category")
public class SaleDTO {
    @Schema(description = "Unique identifier for the sale", required = true)
    private UUID id;

    @Schema(description = "Name of the sale", required = true)
    private String name;

    @Schema(description = "Description of the sale")
    private String description;

    @Schema(name="discount_percentage",description = "Discount percentage for the sale", required = true)
    private double discountPercentage;

    @Schema(name="start_date",description = "Start date of the sale", required = true)
    private String startDate;

    @Schema(name="end_date",description = "End date of the sale", required = true)
    private String endDate;
}
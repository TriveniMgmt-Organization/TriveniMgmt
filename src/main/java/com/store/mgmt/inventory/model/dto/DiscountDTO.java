package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Discount", description = "Data Transfer Object for a product category")
public class DiscountDTO {
    @Schema(description = "Unique identifier for the discount")
    private String id;

    @Schema(description = "Name of the discount")
    private String name;

    @Schema(description = "Description of the discount")
    private String description;

    @Schema(description = "Percentage value of the discount")
    private double percentage;

    @Schema(description = "Start date of the discount validity")
    private String startDate;

    @Schema(description = "End date of the discount validity")
    private String endDate;
}
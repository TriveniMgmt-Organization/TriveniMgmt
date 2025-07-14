package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UpdateSaleItem", description = "Data Transfer Object for a product category")
public class UpdateSaleItemDTO {
    private String id;
    private String name;
    private String description;
    private String categoryId;
    private String unitOfMeasureId;
    private Double price;
    private Integer quantity;
    private Boolean isActive;
}
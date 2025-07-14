package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdateSale", description = "Data Transfer Object for a product category")
public class UpdateSaleDTO extends CreateSaleDTO{
    @Schema(description = "Unique identifier for the sale", required = true)
    private UUID id;

}
package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UpdateSale", description = "Data Transfer Object for a product category")
public class UpdateSaleDTO extends CreateSaleDTO{
    @Schema(description = "Unique identifier for the sale", required = true)
    private UUID id;

}
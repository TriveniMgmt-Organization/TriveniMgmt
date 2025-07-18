package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "update_supplier", description = "Data Transfer Object for a product category")
public class UpdateSupplierDTO extends CreateSupplierDTO{

    @Schema(description = "Unique identifier for the location", required = true)
    private UUID id;
}
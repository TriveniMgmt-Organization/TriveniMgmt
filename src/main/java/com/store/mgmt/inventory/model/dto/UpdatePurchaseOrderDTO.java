package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdatePurchaseOrder", description = "Data Transfer Object for a product category")
public class UpdatePurchaseOrderDTO extends CreatePurchaseOrderDTO {
    @Schema(description = "Unique identifier for the purchase order", required = true)
    private UUID id; // Assuming there's an ID to identify the purchase order
}
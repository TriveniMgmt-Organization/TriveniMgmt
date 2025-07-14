package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdatePurchaseOrderItem", description = "Data Transfer Object for a product category")
public class UpdatePurchaseOrderItemDTO extends CreatePurchaseOrderItemDTO {
    @Schema(description = "Unique identifier for the purchase order item", required = true)
    private UUID id;
}
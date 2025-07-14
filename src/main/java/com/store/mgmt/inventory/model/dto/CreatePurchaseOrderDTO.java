package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "CreatePurchaseOrder", description = "Data Transfer Object for a product category")
public class CreatePurchaseOrderDTO {
    @Schema(description = "Unique identifier for the purchase order", required = true)
    private UUID supplierId;

    @Schema(description = "Location ID where the items will be delivered", required = true)
    private UUID locationId;

@Schema(description = "Unique identifier of the user creating the purchase order", required = true)
    private UUID userId;

    @Schema(description = "Total amount for the purchase order", required = true)
    private double totalAmount;

    @Schema(description = "Status of the purchase order", required = true)
    private String status;

    @Schema(description = "List of items in the purchase order", required = true)
    private PurchaseOrderItemDTO[] items;
}
package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(name = "create_purchase_order", description = "Data Transfer Object for a product category")
public class CreatePurchaseOrderDTO {
    @Schema(name="supplier_id", description = "Unique identifier for the purchase order", required = true)
    private UUID supplierId;

    @Schema(name="location_id", description = "Location ID where the items will be delivered", required = true)
    private UUID locationId;

    @Schema(name="product_template_id", description = "Location ID where the items will be delivered", required = true)
    private UUID productTemplateId;

@Schema(name="user_id", description = "Unique identifier of the user creating the purchase order", required = true)
    private UUID userId;

    @Schema(name="total_amount", description = "Total amount for the purchase order", required = true, minimum = "0", maximum = "1000000000")
    private double totalAmount;

    @Schema(description = "Status of the purchase order", required = true)
    private String status;

    @Schema(description = "List of items in the purchase order", required = true)
    private List<CreatePurchaseOrderDTO> items;
}
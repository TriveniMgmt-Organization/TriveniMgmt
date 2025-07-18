package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "purchase_order", description = "Data Transfer Object for a product category")
public class PurchaseOrderDTO {
    @Schema(description = "Unique identifier for the purchase order", required = true)
    private String id; // Assuming there's an ID to identify the purchase order

    @Schema(name="supplier_name", description = "Name of the supplier", required = true)
    private String supplierName;

    @Schema(name="total_amount", description = "Total amount of the purchase order", required = true, minimum = "0", maximum = "1000000000")
    private double totalAmount;

    @Schema(description = "Status of the purchase order", required = true)
    private String status;

    @Schema(name="created_date", description = "Date when the purchase order was created", required = true)
    private String createdDate;
}
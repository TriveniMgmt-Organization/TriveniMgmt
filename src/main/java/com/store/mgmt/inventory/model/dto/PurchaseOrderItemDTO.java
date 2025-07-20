package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(name = "purchase_order_item", description = "Data Transfer Object for a product category")
public class PurchaseOrderItemDTO {
    @Schema(description = "Unique identifier for the purchase order item", required = true)
    private UUID id;

    @Schema(name = "purchase_order_id",description = "Unique identifier for the purchase order", required = true)
    private UUID purchaseOrderId;

    @Schema(name="product_template_id",description = "Unique identifier of the product in the purchase order item", required = true)
    private UUID productTemplateId;

    @Schema(description = "Quantity of the product in the purchase order item", required = true, minimum = "0", maximum = "1000000")
    private Integer quantity;

    @Schema(description = "Price of the product in the purchase order item", required = true)
    private Double price;

    @Schema(name="cost_price", description = "Cost price of the inventory item", required = true, minimum = "0.01", maximum = "1000000.00")
    private BigDecimal costPrice;

    @Schema(name="retail_price", description = "Retail price of the inventory item", required = true, minimum = "0.01", maximum = "1000000.00")
    private BigDecimal retailPrice;

    @Schema(name="batch_number", description = "Batch number of the product in the purchase order item", required = true)
    private String batchNumber;

    @Schema(name="lot_number", description = "Lot number of the inventory item, used for tracking specific lots")
    private String lotNumber; // Nullable, for tracking specific lots

    @Schema(
            name="expiration_date",
            description = "Expiration date of the product in the purchase order item",
            example = "2023-12-31T23:59:59",
            required = true
    )
    private LocalDateTime expirationDate;

    @Schema(
            name="location_id",
            description = "Unique identifier of the Product of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID locationId;
}
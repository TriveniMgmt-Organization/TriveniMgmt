package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(name = "CreateInventoryItem", description = "Data Transfer Object for a product category")
@AllArgsConstructor
@NoArgsConstructor
public class CreateInventoryItemDTO {
    @Schema(
            description = "Unique identifier of the Product of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID productTemplateId;

    @Schema(
            description = "Unique identifier of the Product of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID locationId;

    @Schema(description = "Quantity of the inventory item", required = true, minimum = "0", maximum = "1000000")
    private int quantity;

    @Schema( description = "Cost price of the inventory item", required = true, minimum = "0.01", maximum = "1000000.00")
    private BigDecimal costPrice;

    @Schema( description = "Retail price of the inventory item", required = true, minimum = "0.01", maximum = "1000000.00")
    private BigDecimal retailPrice;

    @Schema( description = "Quantity of the inventory item", required = true)
    private LocalDateTime expirationDate;

    @Schema( description = "Quantity of the inventory item", required = true)
    private String batchNumber;

@Schema( description = "Lot number of the inventory item, used for tracking specific lots")
    private String lotNumber; // Nullable, for tracking specific lots

//    @Schema(description = "Unit of measure for the inventory item")
//    private String unitOfMeasure;

}
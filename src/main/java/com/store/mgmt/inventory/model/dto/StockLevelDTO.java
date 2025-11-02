package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "StockLevel", description = "Calculated stock levels for an inventory item (derived from StockTransactions)")
public class StockLevelDTO {

    @Schema(
            description = "Unique identifier of the stock level record",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Unique identifier of the inventory item",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID inventoryItemId;

    @Schema(
            description = "Quantity on hand (total physical stock)",
            example = "100",
            minimum = "0"
    )
    private int onHand;

    @Schema(
            description = "Committed quantity (reserved for orders/sales)",
            example = "20",
            minimum = "0"
    )
    private int committed;

    @Schema(
            description = "Available quantity (onHand - committed)",
            example = "80",
            minimum = "0"
    )
    private int available;

    @Schema(description = "Low stock threshold warning", example = "10")
    private int lowStockThreshold;

    @Schema(description = "Maximum stock level (optional)", example = "500")
    private Integer maxStockLevel;

    @Schema(description = "Whether stock is below threshold", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean isLowStock;
}


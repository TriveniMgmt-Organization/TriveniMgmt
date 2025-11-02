package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "InventoryItem", description = "Data Transfer Object for an inventory item (specific stock at a location)")
public class InventoryItemDTO {
    
    @Schema(
            description = "Unique identifier for the inventory item",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Product variant associated with this inventory item",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID variantId;

    @Schema(description = "Product variant details", nullable = true)
    private ProductVariantDTO variant;

    @Schema(
            description = "Location where this inventory item is stored",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID locationId;

    @Schema(description = "Location details", nullable = true)
    private LocationDTO location;

    @Schema(description = "Batch/lot associated with this inventory item")
    private UUID batchLotId;

    @Schema(description = "Batch/lot details", nullable = true)
    private BatchLotDTO batchLot;

    @Schema(description = "Expiry date for this inventory item")
    private LocalDate expiryDate;

    @Schema(description = "Current stock level for this inventory item", nullable = true)
    private StockLevelDTO stockLevel;
}

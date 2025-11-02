package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "StockTransaction", description = "Immutable record of stock movement (all quantity changes go through this)")
public class StockTransactionDTO {

    @Schema(
            description = "Unique identifier of the transaction",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Unique identifier of the inventory item",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID inventoryItemId;

    @Schema(description = "Type of transaction (RECEIPT, SALE, ADJUSTMENT, DAMAGE, TRANSFER_IN, TRANSFER_OUT, COUNT)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(
            description = "Quantity change (positive for receipts, negative for sales)",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int quantityDelta;

    @Schema(description = "Reference to related entity (PO ID, Sale ID, Transfer ID, etc.)")
    private String reference;

    @Schema(description = "User who created this transaction", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Timestamp when transaction was created", accessMode = Schema.AccessMode.READ_ONLY)
        private LocalDateTime timestamp;
}


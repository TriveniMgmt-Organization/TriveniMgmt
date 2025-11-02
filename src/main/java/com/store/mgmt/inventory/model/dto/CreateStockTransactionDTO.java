package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CreateStockTransaction", description = "Data Transfer Object for creating a stock transaction (immutable stock movement)")
public class CreateStockTransactionDTO {

    @Schema(
            description = "Unique identifier of the inventory item",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID inventoryItemId;

    @Schema(
            description = "Type of transaction",
            example = "RECEIPT",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"RECEIPT", "SALE", "ADJUSTMENT", "DAMAGE", "TRANSFER_IN", "TRANSFER_OUT", "COUNT"}
    )
    private String type;

    @Schema(
            description = "Quantity change (positive for receipts/transfers in, negative for sales/transfers out)",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int quantityDelta;

    @Schema(description = "Reference to related entity (PO ID, Sale ID, Transfer ID, etc.)")
    private String reference;
}


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
@Schema(name = "BatchLot", description = "Data Transfer Object for batch/lot tracking")
public class BatchLotDTO {

    @Schema(
            description = "Unique identifier of the batch/lot",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Batch number (unique identifier for the batch)",
            example = "BATCH-2024-001",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 100
    )
    private String batchNumber;

    @Schema(description = "Manufacture date of the batch")
    private LocalDate manufactureDate;

    @Schema(description = "Expiry date of the batch")
    private LocalDate expiryDate;

    @Schema(description = "Unique identifier of the supplier for this batch")
    private UUID supplierId;

    @Schema(description = "Supplier details", nullable = true)
    private SupplierDTO supplier;
}


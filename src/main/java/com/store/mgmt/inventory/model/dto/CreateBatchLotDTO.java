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
@Schema(name = "CreateBatchLot", description = "Data Transfer Object for creating a batch/lot")
public class CreateBatchLotDTO {

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
}


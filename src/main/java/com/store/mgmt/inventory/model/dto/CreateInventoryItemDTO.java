package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CreateInventoryItem", description = "Data Transfer Object for creating an inventory item")
public class CreateInventoryItemDTO {

    @NotNull(message = "Product variant ID is required")
    @Schema(
            description = "Unique identifier of the product variant",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID variantId;

    @NotNull(message = "Location ID is required")
    @Schema(
            description = "Unique identifier of the location",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID locationId;

    @Schema(description = "Batch/lot identifier (optional, for batch tracking). If not provided, a batch lot will be auto-generated.")
    private UUID batchLotId;

    @Size(max = 100, message = "Batch number must be 100 characters or less")
    @Schema(description = "Custom batch number (optional). If provided, will be used instead of auto-generation. Format: any string (e.g., supplier-provided lot number)")
    private String customBatchNumber;

    @Schema(description = "Expiry date for this inventory item (optional)")
    private LocalDate expiryDate;

    @Min(value = 0, message = "Initial quantity cannot be negative")
    @Schema(description = "Initial stock quantity (optional, defaults to 0)", example = "100")
    private Integer initialQuantity;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    @Schema(description = "Low stock threshold for alerts (optional, defaults to 10)", example = "10")
    private Integer lowStockThreshold;
}

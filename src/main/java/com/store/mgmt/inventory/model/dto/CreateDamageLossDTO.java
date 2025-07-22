
package com.store.mgmt.inventory.model.dto;

import com.store.mgmt.inventory.model.entity.DamageLoss;
import com.store.mgmt.inventory.model.enums.DamageLossReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "CreateDamageLoss", description = "Data Transfer Object for a product category")
public class CreateDamageLossDTO {

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

@Schema(
            description = "Reason for the damage or loss",
            required = true,
            example = "DAMAGE" // Example value, can be adjusted based on actual reasons
    )
    private DamageLossReason reason;

    @Schema(
            description = "Unique identifier of the Product of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true // Assuming a product must always belong to a category
    )
    private UUID userId;

    @Schema(description = "Quantity of the product sold", required = true, minimum = "0", maximum = "1000000")
    private int quantity;

    @Schema(description = "Percentage of loss incurred", required = true)
    private double lossPercentage;

    @Schema( description = "Date when the damage/loss occurred", required = true)
    private String dateOfLoss;
}
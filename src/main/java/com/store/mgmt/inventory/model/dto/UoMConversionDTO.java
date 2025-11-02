package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UoMConversion", description = "Data Transfer Object for Unit of Measure conversions")
public class UoMConversionDTO {

    @Schema(
            description = "Unique identifier of the conversion",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private UUID id;

    @Schema(
            description = "Unique identifier of the 'from' unit of measure",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID fromUomId;

    @Schema(description = "'From' unit of measure details", nullable = true)
    private UnitOfMeasureDTO fromUom;

    @Schema(
            description = "Unique identifier of the 'to' unit of measure",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID toUomId;

    @Schema(description = "'To' unit of measure details", nullable = true)
    private UnitOfMeasureDTO toUom;

    @Schema(
            description = "Conversion ratio (1 unit of 'from' = ratio units of 'to')",
            example = "12.0",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.0001"
    )
    private BigDecimal ratio;

    @Schema(description = "Example: 1 Box = 12 Each", accessMode = Schema.AccessMode.READ_ONLY)
    private String description;
}


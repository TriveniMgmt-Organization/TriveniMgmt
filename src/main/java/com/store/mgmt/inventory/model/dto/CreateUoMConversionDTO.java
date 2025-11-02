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
@Schema(name = "CreateUoMConversion", description = "Data Transfer Object for creating a Unit of Measure conversion")
public class CreateUoMConversionDTO {

    @Schema(
            description = "Unique identifier of the 'from' unit of measure",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID fromUomId;

    @Schema(
            description = "Unique identifier of the 'to' unit of measure",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID toUomId;

    @Schema(
            description = "Conversion ratio (1 unit of 'from' = ratio units of 'to')",
            example = "12.0",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.0001"
    )
    private BigDecimal ratio;
}


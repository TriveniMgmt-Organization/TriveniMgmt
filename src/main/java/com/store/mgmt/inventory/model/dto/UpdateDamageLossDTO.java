package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UpdateDamageLoss", description = "Data Transfer Object for a product category")
public class UpdateDamageLossDTO extends CreateDamageLossDTO {
    @Schema(description = "Unique identifier for the damage/loss record", required = true)
    private UUID id;
}
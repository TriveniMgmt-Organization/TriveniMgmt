package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UpdateBrand", description = "Data Transfer Object for a product item available for sale")
public class UpdateBrandDTO extends CreateBrandDTO {
}
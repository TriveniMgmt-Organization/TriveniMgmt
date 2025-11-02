package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UpdateStore", description = "Data Transfer Object for a organization account")
public class UpdateStoreDTO extends CreateStoreDTO {
}

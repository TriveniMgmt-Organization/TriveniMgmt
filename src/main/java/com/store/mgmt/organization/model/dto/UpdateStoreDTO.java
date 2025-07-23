package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UpdateStore", description = "Data Transfer Object for a organization account")
public class UpdateStoreDTO extends CreateStoreDTO {
}

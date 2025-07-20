package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "tenant", description = "Data Transfer Object for a organization account")
public class TenantDTO {
    @Schema(
            description = "Unique identifier of the tenant",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID organizationId;
    @Schema(
            description = "Unique identifier of the store",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID storeId;
}

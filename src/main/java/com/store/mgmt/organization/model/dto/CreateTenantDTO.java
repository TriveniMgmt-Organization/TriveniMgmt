package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "CreateTenant", description = "Data Transfer Object for selecting an organization/store tenant context")
public class CreateTenantDTO {

    @Schema(
            description = "Unique identifier of the organization",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Schema(
            description = "Unique identifier of the store (optional)",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID storeId;
}

package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "CreateUserAssignment", description = "Data Transfer Object for assigning a user to an organization or store")
public class CreateUserAssignmentDTO {

    @Schema(
            description = "Unique identifier of the organization",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Schema(
            description = "Unique identifier of the store (optional, for store-level assignment)",
            example = "fedcba98-7654-3210-fedc-ba9876543210"
    )
    private UUID storeId;

    @Schema(
            description = "Unique identifier of the user to assign",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Schema(
            description = "Unique identifier of the role to assign",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Role ID is required")
    private UUID roleId;
}

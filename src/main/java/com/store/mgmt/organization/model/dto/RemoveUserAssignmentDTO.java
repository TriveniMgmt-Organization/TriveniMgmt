package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "RemoveUserAssignment", description = "Data Transfer Object for removing a user from an organization or store")
public class RemoveUserAssignmentDTO {

    @Schema(
            description = "Unique identifier of the organization",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Schema(
            description = "Unique identifier of the store (optional, for store-level removal)",
            example = "fedcba98-7654-3210-fedc-ba9876543210"
    )
    private UUID storeId;

    @Schema(
            description = "Unique identifier of the user to remove",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "User ID is required")
    private UUID userId;
}

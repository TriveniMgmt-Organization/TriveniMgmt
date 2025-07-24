package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "RemoveUserAssignment", description = "Data Transfer Object for a organization account")
public class RemoveUserAssignmentDTO {
    @Schema(
            description = "Unique identifier of the organization",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID organizationId;

    @Schema(
            description = "Unique identifier of the organization",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.AUTO
    )
    private UUID storeId;

    @Schema(
            description = "Unique identifier of the user",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID userId;
}

package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "create-user-assignment", description = "Data Transfer Object for a organization account")
public class CreateUserAssignmentDTO {
    @Schema(
            name="organization_id",
            description = "Unique identifier of the organization",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID organizationId;

    @Schema(
            name="store_id",
            description = "Unique identifier of the organization",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.AUTO
    )
    private UUID storeId;

    @Schema(
            name="user_id",
            description = "Unique identifier of the user",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID userId;

    @Schema(
            name="role_id",
            description = "Unique identifier of the organization",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID roleId;
}

package com.store.mgmt.organization.model.dto;

import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.users.model.dto.RoleDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UserAssignment", description = "Data Transfer Object for a organization account")
public class UserAssignmentDTO {
    @Schema(
            description = "Unique identifier of the user",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID id;

    @Schema(
            description = "Organization",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrganizationDTO organization;

    @Schema(
            description = "Organization",
            requiredMode = Schema.RequiredMode.AUTO
    )
    private StoreDTO store;

    @Schema(
            description = "Organization",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private RoleDTO role;
}

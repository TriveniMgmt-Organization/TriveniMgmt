package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "Role", description = "Data Transfer Object for a user role")
public class RoleDTO {

    @Schema(
            description = "Unique identifier of the role",
            example = "00a1b2c3-d4e5-f678-9012-34567890abcd"
    )
    private UUID id;

    @Schema(
            description = "Name of the role (e.g., 'ADMIN', 'USER', 'MANAGER')",
            example = "USER",
            minLength = 2,
            maxLength = 30
    )
    private String name;

    @Schema(
            description = "Description of the role's permissions or purpose",
            example = "Standard user role with basic access"
    )
    private String description;

    @ArraySchema(
            schema = @Schema(required = true,implementation = PermissionDTO.class),
            arraySchema = @Schema(description = "Set of permissions assigned to the user")
    )
    private Set<PermissionDTO> permissions;
}
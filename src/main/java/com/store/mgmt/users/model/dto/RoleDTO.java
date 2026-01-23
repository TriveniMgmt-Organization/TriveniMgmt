package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

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
            example = "STORE_MANAGER",
            minLength = 2,
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String name;

    @Schema(
            description = "Description of the role's permissions or purpose",
            example = "Standard user role with basic access"
    )
    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @ArraySchema(
            schema = @Schema(implementation = PermissionDTO.class),
            arraySchema = @Schema(description = "Set of permissions assigned to the role")
    )
    private Set<PermissionDTO> permissions;
}
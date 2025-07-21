package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "CreatePermission", description = "Data Transfer Object for a user role")
public class CreatePermissionDTO {
    @Schema(
            description = "Unique identifier of the role",
            example = "00a1b2c3-d4e5-f678-9012-34567890abcd"
    )
    private UUID id;
    @Schema(
            description = "Name of the permission (e.g., 'ADMIN', 'USER', 'MANAGER')",
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
}
package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(name = "User", description = "Data Transfer Object for a user account")
public class UserDTO {

    @Schema(
            description = "Unique identifier of the user",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID id;

    @Schema(
            description = "Unique username for the user",
            example = "john.doe@example.com",
            minLength = 3,
            maxLength = 50,
            format = "email"
    )
    private String username;

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "email"
    )
    private String email;

    @Schema(
            description = "Full name of the user",
            example = "john doe",
            name = "full_name"
    )
    private String fullName;


    @Schema(
            name="is_active",
            description = "Status indicating if the user account is active",
            example = "true"
    )
    private boolean isActive;

    @ArraySchema(
            schema = @Schema(required = true, implementation = RoleDTO.class),
            arraySchema = @Schema(description = "Set of roles assigned to the user")
    )
    private Set<RoleDTO> roles;

    @ArraySchema(
            schema = @Schema(required = true,implementation = PermissionDTO.class),
            arraySchema = @Schema(description = "Set of permissions assigned to the user")
    )
    private Set<PermissionDTO> permissions;
}
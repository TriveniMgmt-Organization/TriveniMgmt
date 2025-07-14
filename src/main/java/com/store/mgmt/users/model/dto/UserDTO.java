package com.store.mgmt.users.model.dto;

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
            example = "john doe"
    )
    private String fullName;

    @Schema(
            description = "Timestamp when the user account was created (ISO 8601 format)",
            example = "2024-01-15T10:30:00.123456"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Timestamp when the user account was last updated (ISO 8601 format)",
            example = "2024-07-13T22:09:18.789012"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Status indicating if the user account is active",
            example = "true"
    )
    private boolean isActive;

    @Schema(
            description = "Set of roles assigned to the user",
            implementation = RoleDTO.class // Important for documenting the type of elements in the Set
    )
    private Set<RoleDTO> roles;
}
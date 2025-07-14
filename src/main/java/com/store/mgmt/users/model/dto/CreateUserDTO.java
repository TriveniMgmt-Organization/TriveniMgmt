package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(name = "CreateUser", description = "Data Transfer Object for a user account")
public class CreateUserDTO {

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
            format = "email",
            required= true
    )
    private String email;

    @Schema(
            description = "Full name of the user",
            example = "john doe"
    )
    private String fullName;

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
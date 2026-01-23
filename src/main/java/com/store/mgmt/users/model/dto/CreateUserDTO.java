package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(name = "CreateUser", description = "Data Transfer Object for creating a user account")
public class CreateUserDTO {

    @Schema(
            description = "Unique username for the user (typically same as email)",
            example = "john.doe@example.com",
            minLength = 3,
            maxLength = 100
    )
    @Size(max = 100, message = "Username cannot exceed 100 characters")
    private String username;

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Schema(
            description = "First name of the user",
            example = "John"
    )
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Schema(
            description = "Last name of the user",
            example = "Doe"
    )
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Schema(
            description = "Status indicating if the user account is active",
            example = "true"
    )
    private boolean isActive;

    @Schema(
            description = "Set of roles assigned to the user",
            implementation = RoleDTO.class
    )
    private Set<RoleDTO> roles;
}
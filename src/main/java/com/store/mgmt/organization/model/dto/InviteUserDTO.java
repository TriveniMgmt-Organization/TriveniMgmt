package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "InviteUser", description = "Data Transfer Object for inviting a user to an organization")
public class InviteUserDTO {

    @Schema(
            description = "Email address of the user to invite",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Schema(
            description = "Organization ID to invite the user to",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Schema(
            description = "Role name to assign to the user",
            example = "STORE_MANAGER",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name cannot exceed 50 characters")
    private String roleName;

    @Schema(
            description = "Store ID if the invitation is for a specific store (optional)",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID storeId;
}

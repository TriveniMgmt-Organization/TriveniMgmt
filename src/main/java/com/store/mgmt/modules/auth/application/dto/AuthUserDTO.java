package com.store.mgmt.modules.auth.application.dto;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Extended User DTO for authentication responses.
 * Includes the active session context (organization, store, permissions).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AuthUser", description = "Authenticated user with session context")
public class AuthUserDTO {

    @Schema(description = "Unique identifier of the user")
    private UUID id;

    @Schema(description = "Unique username for the user")
    private String username;

    @Schema(description = "Email address of the user", format = "email")
    private String email;

    @Schema(description = "First name of the user")
    private String firstName;

    @Schema(description = "Last name of the user")
    private String lastName;

    @Schema(description = "Profile image URL")
    private String imageUrl;

    @Schema(description = "Whether the user account is active")
    private boolean active;

    @Schema(description = "Currently active organization for this session")
    private OrganizationDTO activeOrganization;

    @Schema(description = "Currently active store for this session")
    private StoreDTO activeStore;

    @Schema(description = "Roles assigned to the user in the active context")
    private Set<String> roles;

    @Schema(description = "Permissions granted to the user in the active context")
    private Set<String> permissions;

    @Schema(description = "When the user account was created")
    private LocalDateTime createdAt;

    @Schema(description = "When the user account was last updated")
    private LocalDateTime updatedAt;
}

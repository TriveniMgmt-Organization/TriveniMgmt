package com.store.mgmt.users.model.dto;

import com.store.mgmt.organization.model.dto.OrganizationDTO;
import com.store.mgmt.organization.model.dto.StoreDTO;
import com.store.mgmt.users.model.PermissionType;
import com.store.mgmt.users.model.RoleType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
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
            description = "First name of the user",
            example = "john",
            name = "firstName"
    )
    private String firstName;

    @Schema(
            description = "Last name of the user",
            example = "doe",
            name = "lastName"
    )
    private String lastName;

    @Schema(
            name="imageUrl",
            description = "Image Url of this product"
    )
    private String imageUrl;

    @Schema(
            name="isActive",
            description = "Status indicating if the user account is active",
            example = "true"
    )
    private boolean isActive;

    @Schema(
            name = "activeOrganization",
            description = "Active organization of the user"
    )
    private OrganizationDTO activeOrganization;

    @Schema(
            name = "activeStore",
            description = "Active organization of the user"
    )
    private StoreDTO activeStore;

    @ArraySchema(
            schema = @Schema(required = true, implementation = RoleType.class),
            arraySchema = @Schema(description = "Set of roles assigned to the user")
    )
    private Set<RoleType> roles;

    @ArraySchema(
            schema = @Schema(required = true,implementation = PermissionType.class),
            arraySchema = @Schema(description = "Set of permissions assigned to the user")
    )
    private Set<PermissionType> permissions;
}

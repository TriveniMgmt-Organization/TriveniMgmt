package com.store.mgmt.organization.model.dto;

import com.store.mgmt.organization.model.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Setter;

import java.util.UUID;

@Data
@Schema(name = "InviteUser", description = "Data Transfer Object for a organization account")
public class InviteUserDTO {
    @Schema(description = "Email of User")
    private String email;

    @Schema(description = "Organization ID",  example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED, format = "uuid")
    private UUID organizationId;

    @Schema(name = "roleName")
    private String roleName;

    @Schema(description = "Store ID if the invitation is for a specific store", example = "123e4567-e89b-12d3-a456-426614174000", format = "uuid")
    private UUID storeId;
}

package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Schema(name = "UpdateRole", description = "Data Transfer Object for a user role")
public class UpdateRoleDTO extends CreateRoleDTO {

    @Schema(
            description = "Unique identifier of the role",
            example = "00a1b2c3-d4e5-f678-9012-34567890abcd"
    )
    private UUID id;
}
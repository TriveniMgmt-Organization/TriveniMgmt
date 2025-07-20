package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "create-organization", description = "Data Transfer Object for a organization account")
public class CreateOrganizationDTO {

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "description"
    )
    private String description;

    @Schema(
            description = "First name of the user",
            example = "john",
            name = "name",
            required = true
    )
    private String name;

    @Schema(
            description = "First name of the user",
            example = "john",
            name = "name"
    )
    private String contactInfo;

    @Schema(
            description = "Unique identifier of the initial admin user",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID initialAdminId;
}

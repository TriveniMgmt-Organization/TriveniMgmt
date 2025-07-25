package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Schema(name = "CreateOrganization", description = "Data Transfer Object for a organization account")
@NoArgsConstructor
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
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
            description = "First name of the user",
            example = "john"
    )
    private String contactInfo;
}

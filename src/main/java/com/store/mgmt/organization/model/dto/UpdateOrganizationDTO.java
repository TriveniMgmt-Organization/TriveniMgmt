package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UpdateOrganization", description = "Data Transfer Object for a organization account")
@NoArgsConstructor
public class UpdateOrganizationDTO extends CreateOrganizationDTO {
    @Schema(
            description = "Unique identifier of the user",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID id;
}

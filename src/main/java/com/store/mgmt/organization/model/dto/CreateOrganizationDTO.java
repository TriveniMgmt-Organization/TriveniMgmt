package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(name = "CreateOrganization", description = "Data Transfer Object for creating an organization")
@NoArgsConstructor
public class CreateOrganizationDTO {

    @Schema(
            description = "Name of the organization",
            example = "Acme Corporation",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;

    @Schema(
            description = "Description of the organization",
            example = "A leading retail company"
    )
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(
            description = "Contact information for the organization",
            example = "+1-555-123-4567"
    )
    @Size(max = 200, message = "Contact info cannot exceed 200 characters")
    private String contactInfo;

    @Schema(
            description = "Optional global template code to apply when creating organization. Use 'CUSTOM' or leave empty for no template.",
            example = "RETAIL_BASIC",
            nullable = true
    )
    @Size(max = 50, message = "Template code cannot exceed 50 characters")
    private String templateCode;
}

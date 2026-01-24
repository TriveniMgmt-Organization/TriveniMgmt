package com.store.mgmt.modules.globaltemplates.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ApplyTemplateRequest", description = "Request to apply a template to an organization")
public class ApplyTemplateRequestDTO {

    @NotNull(message = "Organization ID is required")
    @Schema(description = "ID of the organization to apply the template to", required = true)
    private UUID organizationId;

    @NotBlank(message = "Template code is required")
    @Schema(description = "Code of the template to apply", example = "RETAIL_BASIC", required = true)
    private String templateCode;
}

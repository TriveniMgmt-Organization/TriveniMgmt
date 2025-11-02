package com.store.mgmt.globaltemplates.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "ApplyTemplate", description = "Data Transfer Object for applying a template to an organization")
public class ApplyTemplateDTO {
    
    @Schema(description = "Template code to apply", example = "RETAIL_BASIC", required = true)
    @NotBlank(message = "Template code is required")
    private String templateCode;
    
    @Schema(description = "Organization ID to apply the template to", required = true)
    private UUID organizationId;
}


package com.store.mgmt.modules.globaltemplates.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateTemplateRequest", description = "Request to update a global template")
public class UpdateTemplateRequestDTO {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Name of the template", example = "Retail Starter Pro")
    private String name;

    @Size(max = 50, message = "Type cannot exceed 50 characters")
    @Schema(description = "Type of the template", example = "RETAIL")
    private String type;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the template")
    private String description;

    @Schema(description = "Whether the template is active")
    private Boolean isActive;
}

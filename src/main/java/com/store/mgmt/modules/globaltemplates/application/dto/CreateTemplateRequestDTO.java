package com.store.mgmt.modules.globaltemplates.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateTemplateRequest", description = "Request to create a global template")
public class CreateTemplateRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "Name of the template", example = "Retail Starter", required = true)
    private String name;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    @Schema(description = "Unique code for the template", example = "RETAIL_BASIC", required = true)
    private String code;

    @NotBlank(message = "Type is required")
    @Size(max = 50, message = "Type cannot exceed 50 characters")
    @Schema(description = "Type of the template (RETAIL, GROCERY, PHARMA)", example = "RETAIL", required = true)
    private String type;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the template")
    private String description;

    @Schema(description = "Whether the template is active", defaultValue = "true")
    private Boolean isActive = true;
}

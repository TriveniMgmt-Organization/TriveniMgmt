package com.store.mgmt.globaltemplates.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "CreateGlobalTemplate", description = "Data Transfer Object for creating a global template")
public class CreateGlobalTemplateDTO {
    
    @Schema(description = "Name of the template", example = "Retail Starter", required = true)
    @NotBlank(message = "Template name is required")
    private String name;
    
    @Schema(description = "Unique code for the template", example = "RETAIL_BASIC", required = true)
    @NotBlank(message = "Template code is required")
    private String code;
    
    @Schema(description = "Type of the template", example = "RETAIL", required = true, allowableValues = {"RETAIL", "GROCERY", "PHARMA"})
    @NotBlank(message = "Template type is required")
    private String type;
    
    @Schema(description = "Version of the template", example = "1")
    private Integer version = 1;
    
    @Schema(description = "Whether the template is active", example = "true")
    private Boolean isActive = true;
}


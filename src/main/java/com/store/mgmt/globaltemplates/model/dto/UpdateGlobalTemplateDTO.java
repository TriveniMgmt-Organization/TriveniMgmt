package com.store.mgmt.globaltemplates.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UpdateGlobalTemplate", description = "Data Transfer Object for updating a global template")
public class UpdateGlobalTemplateDTO {
    
    @Schema(description = "Name of the template", example = "Retail Starter")
    private String name;
    
    @Schema(description = "Type of the template", example = "RETAIL")
    private String type;
    
    @Schema(description = "Version of the template", example = "2")
    private Integer version;
    
    @Schema(description = "Whether the template is active", example = "true")
    private Boolean isActive;
}


package com.store.mgmt.globaltemplates.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(name = "GlobalTemplate", description = "Data Transfer Object for a global template")
public class GlobalTemplateDTO {
    
    @Schema(description = "Unique identifier of the template")
    private UUID id;
    
    @Schema(description = "Name of the template", example = "Retail Starter")
    private String name;
    
    @Schema(description = "Unique code for the template", example = "RETAIL_BASIC")
    private String code;
    
    @Schema(description = "Type of the template", example = "RETAIL")
    private String type;
    
    @Schema(description = "Version of the template", example = "1")
    private Integer version;
    
    @Schema(description = "Whether the template is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Items in the template")
    private List<GlobalTemplateItemDTO> items;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}


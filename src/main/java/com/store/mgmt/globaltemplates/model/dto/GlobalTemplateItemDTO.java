package com.store.mgmt.globaltemplates.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "GlobalTemplateItem", description = "Data Transfer Object for a global template item")
public class GlobalTemplateItemDTO {
    
    @Schema(description = "Unique identifier of the template item")
    private UUID id;
    
    @Schema(description = "Template ID this item belongs to")
    private UUID templateId;
    
    @Schema(description = "Type of entity", example = "BRAND", allowableValues = {"BRAND", "CATEGORY", "UOM", "TAX_RULE"})
    private String entityType;
    
    @Schema(description = "JSON data for the entity")
    private JsonNode data;
    
    @Schema(description = "Sort order for display", example = "0")
    private Integer sortOrder;
}


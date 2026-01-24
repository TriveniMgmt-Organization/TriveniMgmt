package com.store.mgmt.modules.globaltemplates.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "GlobalTemplateItemResponse", description = "Global template item response")
public class GlobalTemplateItemResponseDTO {

    @Schema(description = "Unique identifier of the item")
    private UUID id;

    @Schema(description = "Entity type (BRAND, CATEGORY, UOM, etc.)")
    private String entityType;

    @Schema(description = "JSON data for the entity")
    private JsonNode data;

    @Schema(description = "Sort order within the template")
    private Integer sortOrder;
}

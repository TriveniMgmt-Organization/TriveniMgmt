package com.store.mgmt.modules.globaltemplates.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AddTemplateItemRequest", description = "Request to add an item to a template")
public class AddTemplateItemRequestDTO {

    @NotBlank(message = "Entity type is required")
    @Schema(description = "Type of the entity (BRAND, CATEGORY, UOM, etc.)", example = "BRAND", required = true)
    private String entityType;

    @NotBlank(message = "JSON data is required")
    @Schema(description = "JSON data for the entity", required = true)
    private String jsonData;

    @Schema(description = "Sort order within the template", defaultValue = "0")
    private Integer sortOrder = 0;
}

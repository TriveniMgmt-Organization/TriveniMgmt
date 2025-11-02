package com.store.mgmt.globaltemplates.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "CreateTemplateFromJson", description = "DTO for creating a global template from JSON")
public class CreateTemplateFromJsonDTO {
    
    @NotBlank(message = "JSON data is required")
    @Schema(description = "JSON string containing template and items data", example = "{\"template\": {...}, \"items\": [...]}")
    private String jsonData;
}


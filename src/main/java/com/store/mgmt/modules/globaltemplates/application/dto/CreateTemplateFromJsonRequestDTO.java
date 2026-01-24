package com.store.mgmt.modules.globaltemplates.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateTemplateFromJsonRequest", description = "Request to create a template from JSON")
public class CreateTemplateFromJsonRequestDTO {

    @NotBlank(message = "JSON data is required")
    @Schema(description = "JSON string containing the template definition", required = true)
    private String jsonData;
}

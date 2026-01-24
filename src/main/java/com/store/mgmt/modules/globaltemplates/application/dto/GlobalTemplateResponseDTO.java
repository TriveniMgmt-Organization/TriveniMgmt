package com.store.mgmt.modules.globaltemplates.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "GlobalTemplateResponse", description = "Global template response")
public class GlobalTemplateResponseDTO {

    @Schema(description = "Unique identifier of the template")
    private UUID id;

    @Schema(description = "Name of the template", example = "Retail Starter")
    private String name;

    @Schema(description = "Unique code for the template", example = "RETAIL_BASIC")
    private String code;

    @Schema(description = "Type of the template", example = "RETAIL")
    private String type;

    @Schema(description = "Description of the template")
    private String description;

    @Schema(description = "Whether the template is active")
    private Boolean isActive;

    @Schema(description = "Items in the template")
    private List<GlobalTemplateItemResponseDTO> items;

    @Schema(description = "Count of items by entity type")
    private Map<String, Integer> itemCounts;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

package com.store.mgmt.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SelectTenantRequestDTO", description = "Request to select active organization and store")
public class SelectTenantRequestDTO {

    @NotNull(message = "Organization ID is required")
    @Schema(
            description = "ID of the organization to select",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true
    )
    private UUID organizationId;

    @Schema(
            description = "ID of the store to select (optional)",
            example = "123e4567-e89b-12d3-a456-426614174001"
    )
    private UUID storeId;
}

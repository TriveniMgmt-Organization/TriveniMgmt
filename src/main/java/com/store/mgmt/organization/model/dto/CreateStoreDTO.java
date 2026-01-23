package com.store.mgmt.organization.model.dto;

import com.store.mgmt.organization.enums.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "CreateStore", description = "Data Transfer Object for creating a store")
public class CreateStoreDTO {

    @Schema(
            description = "Unique identifier for the organization",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @Schema(
            description = "Name of the store",
            example = "Downtown Store",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Store name is required")
    @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
    private String name;

    @Schema(
            description = "Location of the store",
            example = "123 Main Street, City, State 12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Store location is required")
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Schema(
            description = "Contact information for the store",
            example = "+1-555-123-4567"
    )
    @Size(max = 200, message = "Contact info cannot exceed 200 characters")
    private String contactInfo;

    @Schema(
            description = "Status of the store, indicating its current state",
            example = "ACTIVE",
            defaultValue = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "CLOSED"}
    )
    private StoreStatus status;
}
package com.store.mgmt.organization.model.dto;

import com.store.mgmt.organization.model.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "create-store", description = "Data Transfer Object for a organization account")
public class CreateStoreDTO {

    @Schema(
            description = "Unique identifier for the organization",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID organizationId;

    @Schema(
            description = "First name of the user",
            example = "john",
            name = "name",
            required=true
    )
    private String name;

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "location",
            required=true
    )
    private String location;

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "contactInfo"
    )
    private String contactInfo;

    @Schema(
            description = "Status of the store, indicating its current state",
            example = "ACTIVE",
            defaultValue = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "CLOSED"}
    )
    private Store.StoreStatus status;
}
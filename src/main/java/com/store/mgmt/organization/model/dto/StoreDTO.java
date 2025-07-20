package com.store.mgmt.organization.model.dto;

import com.store.mgmt.organization.model.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "store", description = "Data Transfer Object for a organization account")
public class StoreDTO {
    @Schema(
            description = "Unique identifier of the user",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID id;

    @Schema(
            description = "Unique identifier of the organization"
    )
    private UUID organizationId;

    @Schema(
            description = "First name of the user",
            example = "john",
            name = "name"
    )
    private String name;

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "location"
    )
    private String location;

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "contactInfo"
    )
    private String contactInfo;

    @Schema(
            description = "First name of the user",
            example = "john",
            name = "status"
    )
    private Store.StoreStatus status;
}

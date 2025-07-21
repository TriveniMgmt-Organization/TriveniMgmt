package com.store.mgmt.organization.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(name = "organization", description = "Data Transfer Object for a organization account")
public class OrganizationDTO {

    @Schema(
            description = "Unique identifier of the user",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID id;

    @Schema(
            description = "Unique email address of the user",
            example = "john.doe@example.com",
            format = "description"
    )
    private String description;

    @Schema(
            description = "First name of the user",
            example = "john"
    )
    private String name;

    @Schema(
            description = "First name of the user",
            example = "john"
    )
    private String contactInfo;

    @Schema(
            description = "List of stores associated with the organization",
            example = "[{\"id\": \"a1b2c3d4-e5f6-7890-1234-567890abcdef\", \"name\": \"Store 1\"}, {\"id\": \"b2c3d4e5-f6g7-8901-2345-67890abcdefg\", \"name\": \"Store 2\"}]"
    )
    private List<StoreDTO> stores;
}

package com.store.mgmt.users.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UpdateUser", description = "Data Transfer Object for a user account")
public class UpdateUserDTO extends CreateUserDTO {
    @Schema(
            description = "Unique identifier of the user",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID id;
}
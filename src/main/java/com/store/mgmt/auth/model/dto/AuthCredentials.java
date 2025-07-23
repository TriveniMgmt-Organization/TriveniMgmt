package com.store.mgmt.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "AuthCredentials", description = "Data Transfer Object for a Auth Credential")
public class AuthCredentials {

    @Schema(
            description = "Unique username for the user",
            example = "john.doe",
            minLength = 3,
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Schema(
            description = "User's password",
            example = "MyStrongPassword123!",
            format = "password",
            minLength = 8,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Schema(description = "Remember me option for the user",
            example = "true",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
            )
    private boolean rememberMe;
}

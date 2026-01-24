package com.store.mgmt.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LoginRequestDTO", description = "Login credentials")
public class LoginRequestDTO {

    @Schema(
            description = "Username or email for login",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Schema(
            description = "User's password",
            example = "MyStrongPassword123!",
            format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Schema(
            description = "Remember me option",
            example = "true",
            defaultValue = "false"
    )
    private boolean rememberMe;
}

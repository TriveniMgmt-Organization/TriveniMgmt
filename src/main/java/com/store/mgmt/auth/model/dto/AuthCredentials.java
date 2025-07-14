package com.store.mgmt.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "AuthCredentials", description = "Data Transfer Object for a Auth Crednetial")
public class AuthCredentials {

    @Schema(
            description = "Unique username for the user",
            example = "john.doe",
            minLength = 3,
            maxLength = 50
    )
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Schema(
            description = "User's password",
            example = "MyStrongPassword123!",
            format = "password",
            minLength = 8,
            maxLength = 100,
            required = true,
            writeOnly = true
    )
    @NotBlank(message = "Password cannot be empty")
    private String password;
}

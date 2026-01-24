package com.store.mgmt.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RegisterRequestDTO", description = "User registration request")
public class RegisterRequestDTO {

    @NotBlank(message = "First Name cannot be empty")
    @Size(min = 2, max = 50, message = "First Name must be between 2 and 50 characters")
    @Schema(example = "John", required = true, minLength = 2, maxLength = 50)
    private String firstName;

    @NotBlank(message = "Last Name cannot be empty")
    @Size(min = 2, max = 50, message = "Last Name must be between 2 and 50 characters")
    @Schema(example = "Doe", required = true, minLength = 2, maxLength = 50)
    private String lastName;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Schema(
            description = "Unique email address for the new user",
            example = "john.doe@example.com",
            format = "email",
            required = true,
            maxLength = 100
    )
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    @Schema(
            description = "User's password",
            example = "MySecurePassword123!",
            format = "password",
            required = true,
            minLength = 8,
            maxLength = 100,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String password;

    @NotBlank(message = "Confirm Password cannot be empty")
    @Schema(
            description = "Password confirmation",
            format = "password",
            required = true,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String confirmPassword;

    @Schema(
            description = "Optional invitation token for registration",
            example = "abc123xyz456"
    )
    private String invitationToken;

    @Schema(
            description = "Optional global template code to apply when creating default organization",
            example = "RETAIL_BASIC",
            nullable = true
    )
    private String templateCode;
}

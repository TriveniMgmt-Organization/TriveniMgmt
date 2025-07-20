package com.store.mgmt.auth.model.dto;

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
@Schema(name = "RegisterCredentials", description = "Request object for user registration")
public class RegisterCredentials {

    @NotBlank(message = "First Name cannot be empty")
    @Size(min = 3, max = 50, message = "Full Name must be between 3 and 50 characters")
    @Schema(
            name= "full_name",
            example = "John Doe",
            required = true,
            minLength = 3,
            maxLength = 50
    )
    private String firstName;

    @NotBlank(message = "Last Name cannot be empty")
    @Size(min = 3, max = 50, message = "Last Name must be between 3 and 50 characters")
    @Schema(
            name= "last_name",
            example = "Smith",
            required = true,
            minLength = 3,
            maxLength = 50
    )
    private String lastName;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Schema(
            description = "Unique email address for the new user",
            example = "new.user@example.com",
            format = "email",
            required = true,
            maxLength = 100
    )
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long") // Define strong password requirements
    @Schema(
            description = "User's chosen password",
            example = "MySuperSecretPassword!123", // Example, not a real password
            format = "password",
            required = true,
            minLength = 8,
            maxLength = 100,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String password;

    @Schema(
            name="invitation_token",
            description = "Optional invitation token for registration",
            example = "abc123xyz456"
    )
    private String invitationToken;
}
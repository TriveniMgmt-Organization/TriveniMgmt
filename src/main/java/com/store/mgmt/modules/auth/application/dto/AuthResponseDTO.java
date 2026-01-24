package com.store.mgmt.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AuthResponseDTO", description = "Authentication response containing user details")
public class AuthResponseDTO {

    @Schema(description = "Authenticated user's details with session context", required = true)
    private AuthUserDTO user;

    // Note: Tokens are not exposed in response body for security.
    // They are set as HttpOnly cookies.
}

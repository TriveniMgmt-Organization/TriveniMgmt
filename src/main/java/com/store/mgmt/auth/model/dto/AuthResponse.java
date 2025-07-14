package com.store.mgmt.auth.model.dto;

import  com.store.mgmt.users.model.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AuthResponse", description = "Response object containing authentication token and user details after successful login or registration")
public class AuthResponse {

    @Schema(
            description = "JWT access token for authenticated requests",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            required = true
    )
    private String accessToken;

     @Schema(
             description = "JWT refresh token for obtaining new access tokens",
             example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
             nullable = true
     )
     private String refreshToken;

    @Schema(
            description = "Authenticated user's details",
            required = true
    )
    private UserDTO user;

//     @Schema(
//             description = "Time in milliseconds until the access token expires",
//             example = "3600000" // 1 hour
//     )
//     private Long expiresInMs;
}
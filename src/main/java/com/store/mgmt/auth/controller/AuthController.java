package com.store.mgmt.auth.controller;

import com.store.mgmt.auth.model.dto.*;
import com.store.mgmt.auth.service.AuthService;
import com.store.mgmt.auth.service.JWTService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JWTService jwtService;

    public AuthController( AuthService authService , JWTService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token along with user details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token and user details returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid username or password",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "User account is inactive",
                    content = @Content)
    })
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                       description = "User login credentials",
                                                       required = true,
                                                       content = @Content(schema = @Schema(implementation = AuthCredentials.class))
                                               )
                                               @Valid @RequestBody AuthCredentials request, HttpServletResponse response) {
        logger.info("Received login request for username: {}", request.getUsername());
        AuthResponse authResponse = authService.authenticateUser(request);
        ResponseCookie cookie = ResponseCookie.from("session_token", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and automatically logs them in, returning a JWT token and user details.",
            responses = {
                    @ApiResponse(
                            responseCode = "201", // 201 Created is the standard for successful creation
                            description = "User registered and authenticated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request: Invalid input data (e.g., validation errors)",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409", // 409 Conflict for duplicate resources
                            description = "Conflict: Username or email is already registered",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New user registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterCredentials.class)) // Input DTO
            )
            @Valid @RequestBody RegisterCredentials registrationData) {
        AuthResponse authResponse = authService.registerUser(registrationData);
        // Return 201 CREATED status for successful resource creation
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, null, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validates a JWT token and returns user details if valid.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid, user details returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                    content = @Content)
    })

    public ResponseEntity<AuthResponse> validateToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JWT token to validate",
                    required = true,
                    content = @Content(schema = @Schema(implementation = String.class))
            )
            @RequestBody String token) {
        logger.info("Received token validation request");
        try {
            AuthResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, null, null));
        }
    }
}
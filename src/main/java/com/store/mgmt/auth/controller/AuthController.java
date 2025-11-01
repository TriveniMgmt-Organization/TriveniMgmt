package com.store.mgmt.auth.controller;

import com.store.mgmt.auth.model.dto.*;
import com.store.mgmt.auth.service.AuthService;
import com.store.mgmt.organization.model.dto.OrganizationDTO;
import com.store.mgmt.organization.model.dto.CreateTenantDTO;
import com.store.mgmt.users.model.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.WebUtils;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "AuthController", description = "Endpoints for user authentication")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final String ACCESS_TOKEN_COOKIE_NAME = "session_token";
    private final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
            @Valid @RequestBody AuthCredentials request,
            HttpServletResponse response) {

        logger.info("Received login request for username: {}", request.getUsername());

        try {
            AuthResponse authResponse = authService.authenticateUser(request);
            setAuthCookies(authResponse, response);
            System.out.println("Controller: Response cookies set: " +
                    "Access Token Cookie: " + authResponse.getAccessToken() +
                    ", Refresh Token Cookie: " + authResponse.getRefreshToken());

            // Don't expose tokens in response body for security
            AuthResponse sanitizedResponse = new AuthResponse(
                    null, // Don't send access token in response
                    null, // Don't send refresh token in response
                    authResponse.getUser()
            );

            return ResponseEntity.ok(sanitizedResponse);
        } catch (Exception e) {
            logger.error("Login failed for username: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and automatically logs them in, returning a JWT token and user details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered and authenticated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict: Username or email already exists",
                    content = @Content)
    })
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New user registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterCredentials.class))
            )
            @Valid @RequestBody RegisterCredentials registrationData,
            HttpServletResponse response) {

        try {
            AuthResponse authResponse = authService.registerUser(registrationData);
            setAuthCookies(authResponse, response);

            // Don't expose tokens in response body
            AuthResponse sanitizedResponse = new AuthResponse(
                    null,
                    null,
                    authResponse.getUser()
            );

            return new ResponseEntity<>(sanitizedResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Refreshes the access token using the refresh token from cookies")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = getRefreshTokenFromCookies(request);

            if (refreshToken == null) {
                logger.warn("No refresh token found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            AuthResponse authResponse = authService.refreshToken(refreshToken);
            setAuthCookies(authResponse, response);

            // Return only user data, tokens are in cookies
            AuthResponse sanitizedResponse = new AuthResponse(
                    null,
                    null,
                    authResponse.getUser()
            );

            return ResponseEntity.ok(sanitizedResponse);
        } catch (JwtException e) {
            logger.error("Token refresh failed", e);
            clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the refresh token and clears authentication cookies")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = getRefreshTokenFromCookies(request);
            if (refreshToken != null) {
                authService.logout(refreshToken);
            }
        } catch (Exception e) {
            logger.error("Logout cleanup failed", e);
        }

        clearAuthCookies(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the current authenticated user's information",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access - no valid token found",
                            content = @Content)
            })
    public ResponseEntity<UserDTO> getCurrentUser(HttpServletRequest request) {
        try {
            String accessToken = getAccessTokenFromCookies(request);

            if (accessToken == null) {
                logger.warn("No access token found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserDTO authResponse = authService.getCurrentUser();
            return ResponseEntity.ok(authResponse);
        } catch (JwtException e) {
            logger.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/validate-token")
    @Operation(summary = "Validate access token", description = "Validates the access token from cookies without returning user data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid or missing token",
                    content = @Content)
    })
    public ResponseEntity<Void> validateToken(HttpServletRequest request) {
        try {
            String accessToken = getAccessTokenFromCookies(request);
            if (accessToken == null) {
                logger.warn("No access token found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            authService.validateToken(accessToken);
            return ResponseEntity.ok().build();
        } catch (JwtException e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private void setAuthCookies(AuthResponse authResponse, HttpServletResponse response) {
        boolean isProduction = "production".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
        URI frontendUri = URI.create(frontendUrl);
        String domain = frontendUri.getHost();

        // For local development with ports (like localhost:3000)
        if (!isProduction && domain.startsWith("localhost")) {
            domain = null; // Let browser handle localhost domain
        }
        // Set access token cookie (shorter expiration)
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, authResponse.getAccessToken())
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .domain(domain)
                .maxAge(Duration.ofMinutes(15)) // Short-lived access token
                .sameSite(isProduction ? "Strict" : "Lax")
                .build();

        // Set refresh token cookie (longer expiration)
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .domain(domain)
                .maxAge(Duration.ofDays(7)) // Longer-lived refresh token
                .sameSite(isProduction ? "Strict" : "Lax")
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        boolean isProduction = "production".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
//                .secure("production".equals(System.getenv("SPRING_PROFILES_ACTIVE")))
                .secure(isProduction)
                .path("/")
                .maxAge(0)
                .sameSite(isProduction ? "Strict" : "Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .maxAge(0)
                .sameSite(isProduction ? "Strict" : "Lax")
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    private String getAccessTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        if (cookie != null) {
            return cookie.getValue();
        }
        logger.debug("No access token cookie found");
        return null;
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
        if (cookie != null) {
            return cookie.getValue();
        }
        logger.debug("No refresh token cookie found");
        return null;
    }

    @GetMapping("/organizations")
    @Operation(summary = "Get organizations", description = "Returns a list of organizations the user belongs to")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of organizations returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrganizationDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access",
                    content = @Content)
    })
    public ResponseEntity<List<OrganizationDTO>> getOrganizations() {
        try {
            List<OrganizationDTO> organizations = authService.getOrganizations();
            return ResponseEntity.ok(organizations);
        } catch (Exception e) {
            logger.error("Failed to retrieve organizations", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/select-tenant")
    @Operation(summary = "Select tenant", description = "Sets the current tenant for the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant selected successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid tenant data",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access",
                    content = @Content)
    })
    public ResponseEntity<AuthResponse> selectTenant(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Tenant selection data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateTenantDTO.class))
            )
            @Valid @RequestBody CreateTenantDTO selectDTO,
            HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.selectTenant(selectDTO);
            setAuthCookies(authResponse, response);

            // Don't expose tokens in response body
            AuthResponse sanitizedResponse = new AuthResponse(
                    null,
                    null,
                    authResponse.getUser()
            );

            return ResponseEntity.ok(sanitizedResponse);
        } catch (Exception e) {
            logger.error("Failed to select tenant", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
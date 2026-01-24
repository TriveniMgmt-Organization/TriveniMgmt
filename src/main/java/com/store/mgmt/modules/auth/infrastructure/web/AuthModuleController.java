package com.store.mgmt.modules.auth.infrastructure.web;

import com.store.mgmt.modules.auth.application.command.*;
import com.store.mgmt.modules.auth.application.dto.*;
import com.store.mgmt.modules.auth.application.query.*;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.shared.infrastructure.CommandBus;
import com.store.mgmt.shared.infrastructure.QueryBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Authentication module using Clean Architecture.
 * Uses Command/Query buses to dispatch to handlers.
 */
@RestController
@RequestMapping("/api/v2/auth")
@Tag(name = "Auth Module (v2)", description = "Clean Architecture authentication endpoints")
public class AuthModuleController {

    private static final Logger log = LoggerFactory.getLogger(AuthModuleController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final AuthCookieService authCookieService;

    public AuthModuleController(CommandBus commandBus, QueryBus queryBus, AuthCookieService authCookieService) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.authCookieService = authCookieService;
    }

    // ==================== Authentication Commands ====================

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and sets JWT tokens as HttpOnly cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "User account is inactive")
    })
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) {
        log.info("Login request for user: {}", request.getUsername());

        try {
            LoginCommand cmd = new LoginCommand(
                    request.getUsername(),
                    request.getPassword(),
                    request.isRememberMe(),
                    response
            );

            AuthResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (DisabledException e) {
            log.warn("Account disabled for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account and automatically logs them in")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletResponse response
    ) {
        log.info("Registration request for email: {}", request.getEmail());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            RegisterCommand cmd = new RegisterCommand(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getInvitationToken(),
                    request.getTemplateCode(),
                    response
            );

            AuthResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Refreshes the access token using the refresh token from cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthResponseDTO> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Token refresh request");

        try {
            String refreshToken = authCookieService.getRefreshToken(request);

            if (refreshToken == null) {
                log.warn("No refresh token found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            RefreshTokenCommand cmd = new RefreshTokenCommand(refreshToken, response);
            AuthResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);

        } catch (JwtException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            authCookieService.clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            authCookieService.clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the refresh token and clears authentication cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("Logout request");

        try {
            String refreshToken = authCookieService.getRefreshToken(request);
            LogoutCommand cmd = new LogoutCommand(refreshToken, response);
            commandBus.dispatch(cmd);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed", e);
            // Still clear cookies even if there's an error
            authCookieService.clearAuthCookies(response);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/select-tenant")
    @Operation(summary = "Select tenant", description = "Sets the current organization and store for the user session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant selected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tenant data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized for this organization")
    })
    public ResponseEntity<AuthResponseDTO> selectTenant(
            @Valid @RequestBody SelectTenantRequestDTO request,
            HttpServletResponse response
    ) {
        log.info("Select tenant request - organization: {}, store: {}",
                request.getOrganizationId(), request.getStoreId());

        try {
            SelectTenantCommand cmd = new SelectTenantCommand(
                    request.getOrganizationId(),
                    request.getStoreId(),
                    response
            );

            AuthResponseDTO result = commandBus.dispatch(cmd);
            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            log.warn("Tenant selection not authorized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid tenant selection: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Tenant selection failed", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== Authentication Queries ====================

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the current authenticated user's information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AuthUserDTO> getCurrentUser(HttpServletRequest request) {
        log.info("Get current user request");

        try {
            String accessToken = authCookieService.getAccessToken(request);
            if (accessToken == null) {
                log.warn("No access token found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            GetCurrentUserQuery query = new GetCurrentUserQuery();
            AuthUserDTO result = queryBus.dispatch(query);
            return ResponseEntity.ok(result);

        } catch (JwtException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (SecurityException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/validate-token")
    @Operation(summary = "Validate access token", description = "Validates the access token from cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing token")
    })
    public ResponseEntity<Void> validateToken(HttpServletRequest request) {
        try {
            String accessToken = authCookieService.getAccessToken(request);
            if (accessToken == null) {
                log.warn("No access token found in cookies");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            ValidateTokenQuery query = new ValidateTokenQuery(accessToken);
            Boolean isValid = queryBus.dispatch(query);

            if (isValid) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/organizations")
    @Operation(summary = "Get user organizations", description = "Returns a list of organizations the user belongs to")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<OrganizationDTO>> getOrganizations() {
        log.info("Get organizations request");

        try {
            GetUserOrganizationsQuery query = new GetUserOrganizationsQuery();
            List<OrganizationDTO> result = queryBus.dispatch(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to retrieve organizations", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

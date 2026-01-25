package com.store.mgmt.config.security;

import com.store.mgmt.shared.infrastructure.security.JWTService;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Cookie Authentication Filter for the new architecture.
 * Extracts JWT from cookies and sets up SecurityContext and TenantContext.
 */
public class JWTCookieAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTCookieAuthenticationFilter.class);

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public JWTCookieAuthenticationFilter(JWTService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = jwtService.extractTokenFromCookie(request);

            if (token != null) {
                authenticateFromToken(token);
            } else {
                logger.debug("No session_token cookie found in request");
            }

            filterChain.doFilter(request, response);
        } finally {
            // Always clear TenantContext after request completes
            TenantContext.clear();
        }
    }

    private void authenticateFromToken(String token) {
        try {
            JWTService.JwtData jwtData = jwtService.extractJwtData(token);
            String email = jwtData.username;
            UUID orgId = jwtData.organizationId;
            UUID storeId = jwtData.storeId;

            // Fetch user (minimal query - just for validation and user ID)
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found for email: " + email));

            if (!jwtService.validateToken(token, user)) {
                logger.warn("Invalid or expired JWT for user: {}", email);
                SecurityContextHolder.clearContext();
                return;
            }

            logger.info("JWT Authentication - user: {}, organizationId={}, storeId={}, authorities count={}",
                    email, orgId, storeId, jwtData.authorities != null ? jwtData.authorities.size() : 0);

            // Use authorities from JWT token (not from database)
            // This is more efficient and ensures consistency with what was granted at login
            Map<String, Object> claims = new HashMap<>();
            claims.put("org_id", orgId != null ? orgId.toString() : null);
            claims.put("store_id", storeId != null ? storeId.toString() : null);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, jwtData.authorities);
            authentication.setDetails(claims);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Set TenantContext for handlers
            TenantContext tenantContext = new TenantContext(orgId, storeId, user.getId(), user.getEmail());
            TenantContext.set(tenantContext);

        } catch (JwtException e) {
            logger.warn("Failed to validate JWT from cookie: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
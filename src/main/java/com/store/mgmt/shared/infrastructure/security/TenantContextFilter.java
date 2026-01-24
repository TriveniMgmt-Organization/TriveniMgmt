package com.store.mgmt.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that extracts tenant context from request headers and JWT claims.
 * Sets the TenantContext for the duration of the request.
 */
@Component
@Order(10)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);

    private static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";
    private static final String HEADER_STORE_ID = "X-Store-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            TenantContext context = extractTenantContext(request);
            if (context != null) {
                TenantContext.set(context);
                log.debug("Tenant context set: org={}, store={}, user={}",
                        context.organizationId(), context.storeId(), context.userId());
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private TenantContext extractTenantContext(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        UUID userId = extractUserId(auth);
        String username = extractUsername(auth);

        if (userId == null) {
            return null;
        }

        UUID organizationId = parseUuidHeader(request, HEADER_ORGANIZATION_ID);
        UUID storeId = parseUuidHeader(request, HEADER_STORE_ID);

        // If not in headers, try to get from JWT claims
        if (organizationId == null && auth.getPrincipal() instanceof Jwt jwt) {
            organizationId = parseUuidClaim(jwt, "organization_id");
        }
        if (storeId == null && auth.getPrincipal() instanceof Jwt jwt) {
            storeId = parseUuidClaim(jwt, "store_id");
        }

        return new TenantContext(organizationId, storeId, userId, username);
    }

    private UUID extractUserId(Authentication auth) {
        if (auth.getPrincipal() instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            if (sub != null) {
                try {
                    return UUID.fromString(sub);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID in JWT subject: {}", sub);
                }
            }
            // Try user_id claim
            return parseUuidClaim(jwt, "user_id");
        }
        return null;
    }

    private String extractUsername(Authentication auth) {
        if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }
        return auth.getName();
    }

    private UUID parseUuidHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID in header {}: {}", headerName, value);
            }
        }
        return null;
    }

    private UUID parseUuidClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);
        if (value != null && !value.isBlank()) {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID in JWT claim {}: {}", claimName, value);
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip for public endpoints
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");
    }
}

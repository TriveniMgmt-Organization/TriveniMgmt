package com.store.mgmt.auth.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.users.model.entity.User;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JWTService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpiration;

    @Getter
    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience:store-api}")
    private String jwtAudience;
    private static final String ACCESS_TOKEN_COOKIE_NAME = "session_token";

    private SecretKey signingKey;

//    public JWTService(@Value("${jwt.secret}") String secret) {
//        if (secret == null || secret.isEmpty()) {
//            log.error("JWT secret is not configured");
//            throw new IllegalStateException("JWT secret is not configured");
//        }
//        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//    }
    @PostConstruct
    public void init() {
        if (this.secret == null || this.secret.isEmpty()) {
            log.error("JWT secret is still not configured after properties loaded.");
            throw new IllegalStateException("JWT secret is not configured!");
        }
        this.signingKey = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        log.info("JWTService initialized with secret. Key length: {} bytes", this.secret.getBytes(StandardCharsets.UTF_8).length);
    }
    // --- Key Management ---
    private SecretKey getSigningKey() {
        return signingKey;
    }

    // --- Token Generation ---
    public String generateAccessToken(User user,
                                      UUID activeOrganizationId, UUID activeStoreId,
                                      List<GrantedAuthority> authoritiesForActiveOrg) {
        return generateToken(user, accessTokenExpiration, false, activeOrganizationId, activeStoreId, authoritiesForActiveOrg);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration, true, null, null, Collections.emptyList());
    }
    private String generateToken(User user, long expiration, boolean isRefreshToken, UUID activeOrganizationId, UUID activeStoreId,
                                 List<GrantedAuthority> authoritiesForActiveOrg
    ) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(jwtIssuer)
                    .audience(jwtAudience)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration));

            if (!isRefreshToken) {
                // 1. Add authorities relevant to the *active* organization/store
                claimsBuilder.claim("authorities", authoritiesForActiveOrg.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

                // 2. Add the active organization ID (MANDATORY for tenant-scoped tokens)
                if (activeOrganizationId == null) {
                    // This indicates an error in the calling logic. An access token should always have a tenant.
                    log.error("Attempted to generate access token for user {} without an active organization ID.", user.getUsername());
                    throw new IllegalArgumentException("Active organization ID must be provided for access token generation.");
                }
                claimsBuilder.claim("organization_id", activeOrganizationId.toString());
                log.info("Access Token for user {} created with active organization_id: {}", user.getUsername(), activeOrganizationId);


                // 3. Add the active store ID (OPTIONAL, depends on the role's scope)
                if (activeStoreId != null) {
                    claimsBuilder.claim("store_id", activeStoreId.toString());
                    log.info("Access Token for user {} also includes active store_id: {}", user.getUsername(), activeStoreId);
                }
                // Add tenant context (organization_id and store_id)
//                Set<UserOrganizationRole> roles = user.getOrganizationRoles();
//                if (!roles.isEmpty()) {
//                    log.info("Generating JWT for user: {} with roles: {}", userDetails.getUsername(), roles);
//                    UserOrganizationRole primaryRole = roles.iterator().next();
//                    if (primaryRole.getOrganization() != null) {
//                        log.info("Primary role organization_id: {}", primaryRole.getOrganization().getId());
//                        claimsBuilder.claim("organization_id", primaryRole.getOrganization().getId().toString());
//                    }
//                    if (primaryRole.getStore() != null) {
//                        log.info("Primary role store_id: {}", primaryRole.getStore().getId());
//                        claimsBuilder.claim("store_id", primaryRole.getStore().getId().toString());
//                    }
//                }
            }

      JWTClaimsSet claims = claimsBuilder.build();
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new MACSigner(signingKey));
            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Failed to generate JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public String refreshAccessToken(String refreshToken, UserDetails userDetails, User user) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(refreshToken);
            if (!signedJWT.verify(new MACVerifier(signingKey))) {
                log.warn("Invalid refresh token signature");
                throw new JwtException("Invalid refresh token");
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String username = claims.getSubject();
            if (!username.equals(userDetails.getUsername())) {
                log.warn("Refresh token username mismatch for user: {}", username);
                throw new JwtException("Refresh token username mismatch");
            }
            if (claims.getExpirationTime().before(new Date())) {
                log.warn("Refresh token expired for user: {}", username);
                throw new JwtException("Refresh token expired");
            }
            return generateAccessToken(user, null, null, Collections.emptyList() );
        } catch (Exception e) {
            log.error("Failed to refresh access token: {}", e.getMessage(), e);
            throw new JwtException("Failed to refresh access token", e);
        }
    }
  
    // --- Claim Extraction ---
    private JWTClaimsSet parseAndGetClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // You might want to add signature verification here if validateToken doesn't already do it comprehensively
            // And consider issuer/expiration checks if validateToken is simple
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            // Use a more specific exception if possible, or handle logging here
            throw new JwtException("Invalid token format or parsing error", e);
        }
    }


    public UUID extractOrganizationId(JWTClaimsSet claims) {
        String orgId = (String) claims.getClaim("organization_id");
        return orgId != null ? UUID.fromString(orgId) : null;
    }

    public UUID extractStoreId(JWTClaimsSet claims) {
        String storeId = (String) claims.getClaim("store_id");
        return storeId != null ? UUID.fromString(storeId) : null;
    }

    // You might also want a method that returns a container object for all relevant JWT data
    public static class JwtData {
        public String username;
        public UUID organizationId;
        public UUID storeId;
    }

    public JwtData extractJwtData(String token) {
        JWTClaimsSet claims = parseAndGetClaims(token);
        JwtData data = new JwtData();
        data.username = claims.getSubject();
        data.organizationId = (String) claims.getClaim("organization_id") != null ? UUID.fromString((String) claims.getClaim("organization_id")) : null;
        data.storeId = (String) claims.getClaim("store_id") != null ? UUID.fromString((String) claims.getClaim("store_id")) : null;
        
        if (data.username == null) {
            log.warn("JWT does not contain a username");
            throw new JwtException("JWT does not contain a username");
        }
        return data;
    }

    public boolean validateToken(String token, User user) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(signingKey))) {
                log.warn("Invalid token signature for user: {}", user.getUsername());
                return false;
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!claims.getIssuer().equals(jwtIssuer)) {
                log.warn("Invalid issuer for token: expected {}, got {}", jwtIssuer, claims.getIssuer());
                return false;
            }
            if (!claims.getAudience().contains(jwtAudience)) {
                log.warn("Invalid audience for token: expected {}, got {}", jwtAudience, claims.getAudience());
                return false;
            }
            if (claims.getExpirationTime().before(new Date())) {
                log.warn("Token expired for user: {}", user.getUsername());
                return false;
            }
            String username = claims.getSubject();
            if (!username.equals(user.getUsername())) {
                log.warn("Token username mismatch: expected {}, got {}", user.getUsername(), username);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed for user: {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }


    public String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.debug("Cookie Name: {}, Cookie Value: {}", cookie.getName(), cookie.getValue());
            }
        } else {
            log.debug("No cookies found in the request");
        }

        Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        if (cookie != null) {
            log.debug("Found session_token cookie with value: {}", cookie.getValue());
            return cookie.getValue();
        } else {
            log.debug("session_token cookie not found");
            return null;
        }
    }

    @Transactional
    public UserDetails createUserDetails(User user) {
        // Include both roles (with ROLE_ prefix) and permissions (without prefix)
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add roles
        authorities.addAll(user.getOrganizationRoles().stream()
                .map(uor -> new SimpleGrantedAuthority("ROLE_" + uor.getRole().getName()))
                .collect(Collectors.toList()));
        
        // Add permissions from roles
        authorities.addAll(user.getOrganizationRoles().stream()
                .flatMap(uor -> uor.getRole().getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList()));
        log.debug("Authorities for user {}: {}", user.getEmail(), authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                true,
                true,
                true,
                authorities
        );
    }
}

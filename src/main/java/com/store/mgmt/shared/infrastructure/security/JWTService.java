package com.store.mgmt.shared.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Service for token generation and validation.
 * Part of shared infrastructure used by auth module and security filters.
 */
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

    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final JpaRoleRepository roleRepository;

    public JWTService(
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            JpaRoleRepository roleRepository
    ) {
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        if (this.secret == null || this.secret.isEmpty()) {
            log.error("JWT secret is still not configured after properties loaded.");
            throw new IllegalStateException("JWT secret is not configured!");
        }
        this.signingKey = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        log.info("JWTService initialized with secret. Key length: {} bytes", this.secret.getBytes(StandardCharsets.UTF_8).length);
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    public String generateAccessToken(User user,
                                      UUID activeOrganizationId, UUID activeStoreId,
                                      List<GrantedAuthority> authoritiesForActiveOrg) {
        return generateToken(user, accessTokenExpiration, false, activeOrganizationId, activeStoreId, authoritiesForActiveOrg);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration, true, null, null, Collections.emptyList());
    }

    private String generateToken(User user, long expiration, boolean isRefreshToken, UUID activeOrganizationId, UUID activeStoreId,
                                 List<GrantedAuthority> authoritiesForActiveOrg) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(jwtIssuer)
                    .audience(jwtAudience)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration));

            claimsBuilder.claim("user_id", user.getId().toString());

            if (!isRefreshToken) {
                claimsBuilder.claim("authorities", authoritiesForActiveOrg.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

                if (activeOrganizationId == null) {
                    log.error("Attempted to generate access token for user {} without an active organization ID.", user.getUsername());
                    throw new IllegalArgumentException("Active organization ID must be provided for access token generation.");
                }
                claimsBuilder.claim("organization_id", activeOrganizationId.toString());
                log.info("Access Token for user {} created with active organization_id: {}", user.getUsername(), activeOrganizationId);

                if (activeStoreId != null) {
                    claimsBuilder.claim("store_id", activeStoreId.toString());
                    log.info("Access Token for user {} also includes active store_id: {}", user.getUsername(), activeStoreId);
                }
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

    public String refreshAccessToken(String refreshToken, UserDetails userDetails, User user,
                                     UUID activeOrganizationId, UUID activeStoreId,
                                     List<GrantedAuthority> authorities) {
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

            if (activeOrganizationId == null) {
                log.error("Cannot refresh token without active organization ID for user: {}", username);
                throw new JwtException("Active organization ID required for token refresh");
            }

            log.info("Refreshing access token for user: {} with organization: {}", username, activeOrganizationId);
            return generateAccessToken(user, activeOrganizationId, activeStoreId, authorities);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to refresh access token: {}", e.getMessage(), e);
            throw new JwtException("Failed to refresh access token", e);
        }
    }

    private JWTClaimsSet parseAndGetClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
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

    public static class JwtData {
        public String username;
        public UUID organizationId;
        public UUID storeId;
        public List<GrantedAuthority> authorities;
    }

    public JwtData extractJwtData(String token) {
        JWTClaimsSet claims = parseAndGetClaims(token);
        JwtData data = new JwtData();
        data.username = claims.getSubject();
        data.organizationId = (String) claims.getClaim("organization_id") != null ? UUID.fromString((String) claims.getClaim("organization_id")) : null;
        data.storeId = (String) claims.getClaim("store_id") != null ? UUID.fromString((String) claims.getClaim("store_id")) : null;

        // Extract authorities from JWT claims
        @SuppressWarnings("unchecked")
        List<String> authorityStrings = (List<String>) claims.getClaim("authorities");
        if (authorityStrings != null) {
            data.authorities = authorityStrings.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            log.debug("Extracted {} authorities from JWT for user: {}", data.authorities.size(), data.username);
        } else {
            data.authorities = Collections.emptyList();
            log.warn("No authorities found in JWT for user: {}", data.username);
        }

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
                log.debug("Cookie Name: {}, Cookie Value: {}", cookie.getName(), "********");
            }
        } else {
            log.debug("No cookies found in the request");
        }

        Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            log.debug("session_token cookie not found");
            return null;
        }
    }

    @Transactional(readOnly = true)
    public UserDetails createUserDetails(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<UserOrganizationRole> orgRoles = userOrganizationRoleRepository.findByUserId(user.getId());

        // Fetch all roles by IDs
        List<UUID> roleIds = orgRoles.stream()
                .map(UserOrganizationRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        List<Role> roles = roleRepository.findByIdsWithPermissions(roleIds);
        Map<UUID, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        // Add role authorities
        authorities.addAll(orgRoles.stream()
                .map(uor -> roleMap.get(uor.getRoleId()))
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList()));

        // Add permission authorities
        authorities.addAll(orgRoles.stream()
                .map(uor -> roleMap.get(uor.getRoleId()))
                .filter(Objects::nonNull)
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList()));

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

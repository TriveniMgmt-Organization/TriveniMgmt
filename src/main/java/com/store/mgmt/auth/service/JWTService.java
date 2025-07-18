package com.store.mgmt.auth.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.store.mgmt.users.model.entity.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import org.springframework.security.core.GrantedAuthority;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JWTService {
    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience:store-api}")
    private String jwtAudience;

    private SecretKey signingKey;

    public JWTService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isEmpty()) {
            logger.error("JWT secret is not configured");
            throw new IllegalStateException("JWT secret is not configured");
        }
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
    @PostConstruct
    public void init() {
        if (this.secret == null || this.secret.isEmpty()) {
            logger.error("JWT secret is still not configured after properties loaded.");
            throw new IllegalStateException("JWT secret is not configured!");
        }
        this.signingKey = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        logger.info("JWTService initialized with secret. Key length: {} bytes", this.secret.getBytes(StandardCharsets.UTF_8).length);
    }
    // --- Key Management ---
    private SecretKey getSigningKey() {
        return signingKey;
    }

    // --- Token Generation ---
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiration, false);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiration, true);
    }
    private String generateToken(UserDetails userDetails, long expiration, boolean isRefreshToken) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issuer(jwtIssuer)
                    .audience(jwtAudience)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration));

            if (!isRefreshToken) {
                List<String> authorities = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());
                claimsBuilder.claim("authorities", authorities);
            }

      JWTClaimsSet claims = claimsBuilder.build();
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new MACSigner(signingKey));
            return signedJWT.serialize();
        } catch (Exception e) {
            logger.error("Failed to generate JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public String refreshAccessToken(String refreshToken, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(refreshToken);
            if (!signedJWT.verify(new MACVerifier(signingKey))) {
                logger.warn("Invalid refresh token signature");
                throw new JwtException("Invalid refresh token");
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String username = claims.getSubject();
            if (!username.equals(userDetails.getUsername())) {
                logger.warn("Refresh token username mismatch for user: {}", username);
                throw new JwtException("Refresh token username mismatch");
            }
            if (claims.getExpirationTime().before(new Date())) {
                logger.warn("Refresh token expired for user: {}", username);
                throw new JwtException("Refresh token expired");
            }
            return generateAccessToken(userDetails);
        } catch (Exception e) {
            logger.error("Failed to refresh access token: {}", e.getMessage(), e);
            throw new JwtException("Failed to refresh access token", e);
        }
    }
  
    // --- Claim Extraction ---
    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            throw new JwtException("Invalid token", e);
        }
    }

    public boolean validateToken(String token, User user) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(signingKey))) {
                logger.warn("Invalid token signature for user: {}", user.getUsername());
                return false;
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!claims.getIssuer().equals(jwtIssuer)) {
                logger.warn("Invalid issuer for token: expected {}, got {}", jwtIssuer, claims.getIssuer());
                return false;
            }
            if (!claims.getAudience().contains(jwtAudience)) {
                logger.warn("Invalid audience for token: expected {}, got {}", jwtAudience, claims.getAudience());
                return false;
            }
            if (claims.getExpirationTime().before(new Date())) {
                logger.warn("Token expired for user: {}", user.getUsername());
                return false;
            }
            String username = claims.getSubject();
            if (!username.equals(user.getUsername())) {
                logger.warn("Token username mismatch: expected {}, got {}", user.getUsername(), username);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.warn("Token validation failed for user: {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}

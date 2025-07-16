package com.store.mgmt.auth.service;

import com.store.mgmt.users.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.springframework.security.core.GrantedAuthority;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
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
    private final SecretKey signingKey;

    public JWTService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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
        Map<String, Object> claims = new HashMap<>();
        if (!isRefreshToken) {
            // Add authorities (roles/permissions) only to access tokens
            claims.put("authorities", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(jwtIssuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // --- Token Refresh ---
    public String refreshAccessToken(String refreshToken, UserDetails userDetails) {
        Claims claims = extractAllClaims(refreshToken);
        String username = claims.getSubject();
        if (!username.equals(userDetails.getUsername())) {
            logger.warn("Refresh token username mismatch for user: {}", username);
            throw new JwtException("Refresh token username mismatch");
        }
        return generateAccessToken(userDetails);
    }

    // --- Claim Extraction ---
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT: {}", e.getMessage());
            throw new JwtException("Malformed JWT", e);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid JWT argument: {}", e.getMessage());
            throw new JwtException("Invalid JWT argument", e);
        } catch (Exception e) {
            logger.error("Unknown JWT parsing error: {}", e.getMessage(), e);
            throw new JwtException("Unknown JWT parsing error", e);
        }
    }

    // --- Token Validation ---
    public boolean validateToken(String token, User userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            boolean isValid = username.equals(userDetails.getUsername());
            if (!isValid) {
                logger.warn("Token username mismatch: expected {}, got {}", userDetails.getUsername(), username);
            }
            return isValid;
        } catch (JwtException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
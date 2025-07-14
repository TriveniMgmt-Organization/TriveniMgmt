package com.store.mgmt.auth.service;

import com.store.mgmt.users.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
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

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    // --- Key Management ---
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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
            throw e;
        } catch (MalformedJwtException e) {
            throw new JwtException("Malformed JWT", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Invalid JWT argument", e);
        } catch (Exception e) {
            throw new JwtException("Unknown JWT parsing error", e);
        }
    }

    // --- Token Validation ---
    public boolean validateToken(String token, User userDetails) {
        try {
            final Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            return username.equals(userDetails.getUsername());
        } catch (JwtException e) {
            return false;
        }
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
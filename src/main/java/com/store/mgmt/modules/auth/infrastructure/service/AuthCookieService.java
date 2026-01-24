package com.store.mgmt.modules.auth.infrastructure.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.net.URI;
import java.time.Duration;

/**
 * Service for managing authentication cookies.
 */
@Service
public class AuthCookieService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "session_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Sets authentication cookies (access and refresh tokens).
     */
    public void setAuthCookies(String accessToken, String refreshToken, HttpServletResponse response) {
        boolean isProduction = isProductionEnvironment();
        String domain = getCookieDomain();

        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .domain(domain)
                .maxAge(Duration.ofMinutes(15))
                .sameSite(isProduction ? "Strict" : "Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .domain(domain)
                .maxAge(Duration.ofDays(7))
                .sameSite(isProduction ? "Strict" : "Lax")
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    /**
     * Clears authentication cookies.
     */
    public void clearAuthCookies(HttpServletResponse response) {
        boolean isProduction = isProductionEnvironment();

        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
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

    /**
     * Extracts the access token from request cookies.
     */
    public String getAccessToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * Extracts the refresh token from request cookies.
     */
    public String getRefreshToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    private boolean isProductionEnvironment() {
        return "production".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
    }

    private String getCookieDomain() {
        boolean isProduction = isProductionEnvironment();
        URI frontendUri = URI.create(frontendUrl);
        String domain = frontendUri.getHost();

        // For local development with ports (like localhost:3000)
        if (!isProduction && domain != null && domain.startsWith("localhost")) {
            domain = null; // Let browser handle localhost domain
        }

        return domain;
    }
}

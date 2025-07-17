package com.store.mgmt.config.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

    public class JWTCookieAuthenticationFilter  extends OncePerRequestFilter{

        private static final Logger logger = LoggerFactory.getLogger(JWTCookieAuthenticationFilter.class);
        private static final String ACCESS_TOKEN_COOKIE_NAME = "session_token";
        private final JwtDecoder jwtDecoder;
        private final JwtAuthenticationConverter jwtAuthenticationConverter;

        public JWTCookieAuthenticationFilter(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) {
            this.jwtDecoder = jwtDecoder;
            this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String token = extractTokenFromCookie(request);

            if (token != null) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    JwtAuthenticationToken authentication = (JwtAuthenticationToken) jwtAuthenticationConverter.convert(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authenticated user: {} from cookie token", authentication.getName());
                } catch (JwtException e) {
                    logger.warn("Failed to validate JWT from cookie: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            } else {
                logger.debug("No session_token cookie found in request");
            }

            filterChain.doFilter(request, response);
        }

        private String extractTokenFromCookie(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    logger.debug("Cookie Name: {}, Cookie Value: {}", cookie.getName(), cookie.getValue());
                }
            } else {
                logger.debug("No cookies found in the request");
            }

            Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
            return cookie.getValue();
        }
    }
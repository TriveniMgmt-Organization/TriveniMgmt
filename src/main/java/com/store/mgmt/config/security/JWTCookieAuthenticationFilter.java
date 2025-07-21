package com.store.mgmt.config.security;


import com.store.mgmt.auth.service.JWTService;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.stream.Collectors;

public class JWTCookieAuthenticationFilter  extends OncePerRequestFilter{

    private static final Logger logger = LoggerFactory.getLogger(JWTCookieAuthenticationFilter.class);
    private final JWTService jwtService;
    private final UserRepository userRepository;

    public JWTCookieAuthenticationFilter(JWTService jwtService, UserRepository userRepository ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtService.extractTokenFromCookie(request);

        if (token != null) {
            try {
                JWTService.JwtData jwtData = jwtService.extractJwtData(token);
                 String email = jwtData.username;
                 UUID orgId = jwtData.organizationId;
                 UUID storeId = jwtData.storeId;
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("User not found for email: " + email));
                if (jwtService.validateToken(token, user)) {

                    logger.info("Extracted JWT data: username={}, organizationId={}, storeId={}",
                            jwtData.username, jwtData.organizationId, jwtData.storeId);
                    UserDetails userDetails = jwtService.createUserDetails(user);
                    System.out.println("User Details: " + userDetails.getUsername() + ", Authorities: " +
                            userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("org_id", orgId != null ? orgId.toString() : null);
                    claims.put("store_id", storeId != null ? storeId.toString() : null);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    System.out.println("Authentication: " + authentication.getName() + ", Authorities: " +
                            authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
                    authentication.setDetails(claims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());
                    System.out.println("Organization ID: " + orgId + ", Store ID: " + storeId);
                    // Set TenantContext
//                    if (orgId != null) {
//                        Organization organization = organizationRepository.findById(orgId)
//                                .orElseThrow(() -> new IllegalStateException("Organization not found for id: " + orgId));
//                        TenantContext.setCurrentOrganization(organization);
//                    }
//                    if (storeId != null) {
//                        Store store = storeRepository.findById(storeId)
//                                .orElseThrow(() -> new IllegalStateException("Store not found for id: " + storeId));
//                        TenantContext.setCurrentStore(store);
//                    }
//                    TenantContext.setCurrentUser(user);

                    logger.debug("Authenticated user: {} with organization_id: {} and store_id: {}",
                            email, orgId, storeId);
                } else {
                    logger.warn("Invalid or expired JWT for user: {}", email);
                    SecurityContextHolder.clearContext();
                }
            } catch (JwtException e) {
                logger.warn("Failed to validate JWT from cookie: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("No session_token cookie found in request");
        }

        filterChain.doFilter(request, response);
    }


}
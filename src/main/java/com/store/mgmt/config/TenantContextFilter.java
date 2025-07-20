package com.store.mgmt.config;

import com.store.mgmt.auth.service.JWTService;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extract tenant context from JWT (if available)
            String token = jwtService.extractTokenFromCookie(request);
            if (token != null) {
                try {

                    JWTService.JwtData jwtData = jwtService.extractJwtData(token);
                    UUID orgId = jwtData.organizationId;
                    UUID storeId = jwtData.storeId;

                    log.debug("Extracted JWT data: organization_id={}, store_id={}", orgId, storeId);
                    if (orgId != null) {
                        Organization organization = organizationRepository.findById(orgId)
                                .orElseThrow(() -> new IllegalStateException("Organization not found for id: " + orgId));
                        TenantContext.setCurrentOrganization(organization);
                        log.debug("Set TenantContext with organization_id: {}", orgId);
                    }
                    if (storeId != null) {
                        Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new IllegalStateException("Store not found for id: " + storeId));
                        TenantContext.setCurrentStore(store);
                        log.debug("Set TenantContext with store_id: {}", storeId);
                    }
                } catch (Exception e) {
                    log.warn("Failed to set TenantContext from JWT: {}", e.getMessage());
                    // Continue without tenant context for public endpoints
                }
            } else {
                // Optionally extract tenant context from headers for public endpoints
                String orgIdHeader = request.getHeader("X-Organization-Id");
                String storeIdHeader = request.getHeader("X-Store-Id");
                if (orgIdHeader != null) {
                    try {
                        UUID orgId = UUID.fromString(orgIdHeader);
                        Organization organization = organizationRepository.findById(orgId)
                                .orElseThrow(() -> new IllegalStateException("Organization not found for id: " + orgId));
                        TenantContext.setCurrentOrganization(organization);
                        log.debug("Set TenantContext with organization_id from header: {}", orgId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid X-Organization-Id header: {}", orgIdHeader);
                    }
                }
                if (storeIdHeader != null && TenantContext.getCurrentOrganization() != null) {
                    try {
                        UUID storeId = UUID.fromString(storeIdHeader);
                        Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new IllegalStateException("Store not found for id: " + storeId));
                        if (store.getOrganization().getId().equals(TenantContext.getCurrentOrganization().getId())) {
                            TenantContext.setCurrentStore(store);
                            log.debug("Set TenantContext with store_id from header: {}", storeId);
                        } else {
                            log.warn("Store {} does not belong to organization {}", storeId, TenantContext.getCurrentOrganization().getId());
                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid X-Store-Id header: {}", storeIdHeader);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            log.debug("Cleared TenantContext");
        }
    }
}
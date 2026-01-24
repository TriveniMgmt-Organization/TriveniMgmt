package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.application.service.AuthContextService.TokenPair;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for SelectTenantCommand.
 * Changes the active organization/store for the user session.
 */
@Component
@Transactional
public class SelectTenantHandler implements CommandHandler<SelectTenantCommand, AuthResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(SelectTenantHandler.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final AuthContextService authContextService;
    private final AuthCookieService authCookieService;

    public SelectTenantHandler(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            AuthContextService authContextService,
            AuthCookieService authCookieService
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.authContextService = authContextService;
        this.authCookieService = authCookieService;
    }

    @Override
    public AuthResponseDTO handle(SelectTenantCommand cmd) {
        log.info("Selecting tenant - org: {}, store: {}", cmd.organizationId(), cmd.storeId());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch user with roles to avoid N+1
        User user = userRepository.findByUsernameWithAllRelatedData(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        // Validate user has access to the organization
        boolean hasAccess = user.getOrganizationRoles().stream()
                .anyMatch(uor -> uor.getOrganization().getId().equals(cmd.organizationId()));

        if (!hasAccess) {
            throw new SecurityException("User not authorized for this organization");
        }

        // Fetch organization
        var organization = organizationRepository.findById(cmd.organizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        // Fetch and validate store if provided
        Store store = null;
        if (cmd.storeId() != null) {
            store = storeRepository.findById(cmd.storeId())
                    .orElseThrow(() -> new IllegalArgumentException("Store not found"));

            if (!store.getOrganization().getId().equals(cmd.organizationId())) {
                throw new IllegalArgumentException("Store does not belong to the specified organization");
            }
        }

        // Build context and generate tokens
        ActiveContext context = authContextService.buildContextForOrganization(user, organization, store);
        TokenPair tokens = authContextService.generateTokens(user, context);

        // Set cookies
        authCookieService.setAuthCookies(tokens.accessToken(), tokens.refreshToken(), cmd.response());

        log.info("Tenant selected for user: {}", username);

        return AuthResponseDTO.builder()
                .user(authContextService.buildAuthUserDTO(user, context))
                .build();
    }
}

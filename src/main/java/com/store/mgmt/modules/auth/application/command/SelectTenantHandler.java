package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.application.service.AuthContextService.TokenPair;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
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
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    public SelectTenantHandler(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            AuthContextService authContextService,
            AuthCookieService authCookieService,
            UserOrganizationRoleRepository userOrganizationRoleRepository
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.authContextService = authContextService;
        this.authCookieService = authCookieService;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
    }

    @Override
    public AuthResponseDTO handle(SelectTenantCommand cmd) {
        log.info("SelectTenant received - organizationId: {}, storeId: {}, storeId is null: {}",
                cmd.organizationId(), cmd.storeId(), cmd.storeId() == null);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        // Validate user has access to the organization
        boolean hasAccess = userOrganizationRoleRepository.existsByUserIdAndOrganizationId(user.getId(), cmd.organizationId());

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
        log.info("Building context with store object: {}, store ID from object: {}",
                store != null ? "present" : "null", store != null ? store.getId() : "null");
        ActiveContext context = authContextService.buildContextForOrganization(user, organization, store);
        log.info("Built context - orgId: {}, storeId: {}, context.storeId() is null: {}",
                context.organizationId(), context.storeId(), context.storeId() == null);
        TokenPair tokens = authContextService.generateTokens(user, context);
        log.info("Tokens generated successfully");

        // Set cookies
        authCookieService.setAuthCookies(tokens.accessToken(), tokens.refreshToken(), cmd.response());

        log.info("Tenant selected for user: {}", username);

        return AuthResponseDTO.builder()
                .user(authContextService.buildAuthUserDTO(user, context))
                .build();
    }
}

package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.application.service.AuthContextService.TokenPair;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.modules.organization.domain.model.Invitation;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.InvitationRepository;
import com.store.mgmt.modules.organization.domain.service.TenantProvisioningService;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.modules.users.domain.model.RoleType;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.shared.infrastructure.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handler for RegisterCommand.
 * Registers a new user and sets up their organization context.
 */
@Component
@Transactional
public class RegisterHandler implements CommandHandler<RegisterCommand, AuthResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(RegisterHandler.class);

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final TenantProvisioningService tenantProvisioningService;
    private final PasswordEncoder passwordEncoder;
    private final AuthContextService authContextService;
    private final AuthCookieService authCookieService;
    private final AuditLogService auditLogService;

    public RegisterHandler(
            UserRepository userRepository,
            InvitationRepository invitationRepository,
            TenantProvisioningService tenantProvisioningService,
            PasswordEncoder passwordEncoder,
            AuthContextService authContextService,
            AuthCookieService authCookieService,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.tenantProvisioningService = tenantProvisioningService;
        this.passwordEncoder = passwordEncoder;
        this.authContextService = authContextService;
        this.authCookieService = authCookieService;
        this.auditLogService = auditLogService;
    }

    @Override
    public AuthResponseDTO handle(RegisterCommand cmd) {
        log.info("Registering new user: {}", cmd.email());

        // Check if email already exists
        if (userRepository.findByEmail(cmd.email()).isPresent()) {
            throw new IllegalArgumentException("Email '" + cmd.email() + "' is already registered.");
        }

        // Create user
        User user = new User();
        user.setFirstName(cmd.firstName());
        user.setLastName(cmd.lastName());
        user.setUsername(cmd.email());
        user.setEmail(cmd.email());
        user.setPasswordHash(passwordEncoder.encode(cmd.password()));
        user.setActive(true);

        UserOrganizationRole userOrgRole;
        Organization organization;
        Store store;

        if (cmd.invitationToken() != null && !cmd.invitationToken().isEmpty()) {
            // Handle invitation-based registration
            var result = handleInvitationRegistration(cmd, user);
            userOrgRole = result.userOrgRole;
            organization = result.organization;
            store = result.store;
        } else {
            // Create new organization for user
            var result = handleNewOrganizationRegistration(cmd, user);
            userOrgRole = result.userOrgRole;
            organization = result.organization;
            store = result.store;
        }

        User savedUser = userRepository.save(user);

        // Now set the userId and save the UserOrganizationRole
        userOrgRole.setUserId(savedUser.getId());
        tenantProvisioningService.saveRoleAssignment(userOrgRole);

        // Build context and generate tokens
        ActiveContext context = authContextService.buildContextForOrganization(savedUser, organization, store);
        TokenPair tokens = authContextService.generateTokens(savedUser, context);

        // Set cookies
        authCookieService.setAuthCookies(tokens.accessToken(), tokens.refreshToken(), cmd.response());

        logAudit("REGISTER_USER", savedUser.getId(), "User registered: " + savedUser.getEmail());
        log.info("User {} registered successfully", cmd.email());

        return AuthResponseDTO.builder()
                .user(authContextService.buildAuthUserDTO(savedUser, context))
                .build();
    }

    private RegistrationResult handleInvitationRegistration(RegisterCommand cmd, User user) {
        Invitation invitation = invitationRepository.findByTokenAndUsedFalse(cmd.invitationToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invitation token."));

        if (!invitation.getEmail().equals(cmd.email())) {
            throw new IllegalArgumentException("Invitation token does not match email.");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation token has expired.");
        }

        // Create role assignment using provisioning service
        UserOrganizationRole userOrgRole = tenantProvisioningService.createRoleAssignment(
                null, // userId will be set after user is saved
                invitation.getOrganization(),
                invitation.getStore(),
                invitation.getRoleId()
        );

        invitation.setUsed(true);
        invitationRepository.save(invitation);

        log.info("User registered via invitation for organization: {}", invitation.getOrganization().getId());

        return new RegistrationResult(userOrgRole, invitation.getOrganization(), invitation.getStore());
    }

    private RegistrationResult handleNewOrganizationRegistration(RegisterCommand cmd, User user) {
        // Provision new organization with default store
        TenantProvisioningService.ProvisioningResult provisioningResult =
                tenantProvisioningService.provisionNewOrganization(cmd.firstName(), cmd.templateCode());

        // Create SUPER_ADMIN role assignment (userId will be set after user is saved)
        UserOrganizationRole userOrgRole = tenantProvisioningService.createRoleAssignment(
                null,
                provisioningResult.organization(),
                null, // Organization-level role, no specific store
                RoleType.SUPER_ADMIN
        );

        log.info("User registered as SUPER_ADMIN of new organization: {}", provisioningResult.organization().getName());

        return new RegistrationResult(userOrgRole, provisioningResult.organization(), provisioningResult.defaultStore());
    }

    private void logAudit(String action, UUID entityId, String message) {
        try {
            auditLogService.builder()
                    .action(action)
                    .entityId(entityId)
                    .message(message)
                    .log();
        } catch (Exception e) {
            log.error("Failed to log audit entry: {}", e.getMessage());
        }
    }

    private record RegistrationResult(
            UserOrganizationRole userOrgRole,
            Organization organization,
            Store store
    ) {}
}

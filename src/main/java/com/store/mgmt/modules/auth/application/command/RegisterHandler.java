package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.globaltemplates.service.TemplateCopyService;
import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.application.service.AuthContextService.TokenPair;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.organization.enums.StoreStatus;
import com.store.mgmt.organization.model.entity.Invitation;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.InvitationRepository;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
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
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthContextService authContextService;
    private final AuthCookieService authCookieService;
    private final TemplateCopyService templateCopyService;
    private final AuditLogService auditLogService;

    public RegisterHandler(
            UserRepository userRepository,
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            InvitationRepository invitationRepository,
            PasswordEncoder passwordEncoder,
            AuthContextService authContextService,
            AuthCookieService authCookieService,
            TemplateCopyService templateCopyService,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.invitationRepository = invitationRepository;
        this.passwordEncoder = passwordEncoder;
        this.authContextService = authContextService;
        this.authCookieService = authCookieService;
        this.templateCopyService = templateCopyService;
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
        Store store = null;

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

        user.setOrganizationRoles(Set.of(userOrgRole));
        User savedUser = userRepository.save(user);

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

        UserOrganizationRole userOrgRole = new UserOrganizationRole();
        userOrgRole.setUser(user);
        userOrgRole.setOrganization(invitation.getOrganization());
        userOrgRole.setRole(invitation.getRole());
        userOrgRole.setStore(invitation.getStore());

        invitation.setUsed(true);
        invitationRepository.save(invitation);

        log.info("User registered via invitation for organization: {}", invitation.getOrganization().getId());

        return new RegistrationResult(userOrgRole, invitation.getOrganization(), invitation.getStore());
    }

    private RegistrationResult handleNewOrganizationRegistration(RegisterCommand cmd, User user) {
        // Generate unique organization name
        String orgName = cmd.firstName() + "'s Organization";
        String baseOrgName = orgName;
        int suffix = 1;
        while (organizationRepository.findByName(orgName).isPresent()) {
            orgName = baseOrgName + " " + suffix++;
        }

        // Create organization
        Organization organization = new Organization();
        organization.setName(orgName);
        Organization savedOrganization = organizationRepository.save(organization);

        // Create default store
        Store defaultStore = new Store();
        defaultStore.setOrganization(savedOrganization);
        defaultStore.setName("Main Store");
        defaultStore.setLocation("Default Location");
        defaultStore.setStatus(StoreStatus.ACTIVE);
        Store savedStore = storeRepository.save(defaultStore);

        log.info("Created default store '{}' for organization '{}'", savedStore.getName(), savedOrganization.getName());
        logAudit("CREATE_DEFAULT_STORE", savedStore.getId(),
                "Default store created for organization: " + savedOrganization.getName());

        // Apply template if provided
        applyTemplateIfProvided(cmd.templateCode(), savedOrganization);

        // Assign SUPER_ADMIN role
        Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN.toString())
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found."));

        UserOrganizationRole userOrgRole = new UserOrganizationRole();
        userOrgRole.setUser(user);
        userOrgRole.setOrganization(savedOrganization);
        userOrgRole.setRole(superAdminRole);

        log.info("User registered as SUPER_ADMIN of new organization: {}", savedOrganization.getName());

        return new RegistrationResult(userOrgRole, savedOrganization, savedStore);
    }

    private void applyTemplateIfProvided(String templateCode, Organization organization) {
        if (templateCode != null && !templateCode.trim().isEmpty()
                && !templateCode.equalsIgnoreCase("CUSTOM")) {
            try {
                templateCopyService.applyTemplate(organization, templateCode);
                organization.setAppliedTemplateCode(templateCode);
                organizationRepository.save(organization);
                logAudit("APPLY_TEMPLATE", organization.getId(),
                        "Template '" + templateCode + "' applied during registration");
            } catch (Exception e) {
                log.error("Failed to apply template '{}': {}", templateCode, e.getMessage());
                logAudit("APPLY_TEMPLATE_ERROR", organization.getId(),
                        "Failed to apply template '" + templateCode + "': " + e.getMessage());
            }
        }
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

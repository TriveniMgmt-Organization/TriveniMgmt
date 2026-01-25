package com.store.mgmt.modules.organization.domain.service;

import com.store.mgmt.modules.globaltemplates.domain.service.TemplateCopyService;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.RoleType;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.shared.infrastructure.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Domain service for provisioning new tenants (organizations, stores, user assignments).
 * This centralizes all tenant setup logic used during registration and administrative operations.
 */
@Service
@Transactional
public class TenantProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(TenantProvisioningService.class);

    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final RoleRepository roleRepository;
    private final TemplateCopyService templateCopyService;
    private final AuditLogService auditLogService;

    public TenantProvisioningService(
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            RoleRepository roleRepository,
            TemplateCopyService templateCopyService,
            AuditLogService auditLogService
    ) {
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.roleRepository = roleRepository;
        this.templateCopyService = templateCopyService;
        this.auditLogService = auditLogService;
    }

    /**
     * Provision a new organization with a default store.
     *
     * @param ownerFirstName First name of the owner (used to generate org name)
     * @param templateCode   Optional template code to apply
     * @return ProvisioningResult containing the created organization and store
     */
    public ProvisioningResult provisionNewOrganization(String ownerFirstName, String templateCode) {
        // Generate unique organization name
        String baseOrgName = ownerFirstName + "'s Organization";
        String orgName = generateUniqueOrgName(baseOrgName);

        // Create organization
        Organization organization = new Organization();
        organization.setName(orgName);
        Organization savedOrganization = organizationRepository.save(organization);

        log.info("Created new organization: {}", savedOrganization.getName());

        // Create default store
        Store defaultStore = provisionStore(savedOrganization, "Main Store", "Default Location");

        logAudit("CREATE_DEFAULT_STORE", defaultStore.getId(),
                "Default store created for organization: " + savedOrganization.getName());

        // Apply template if provided
        applyTemplateIfProvided(templateCode, savedOrganization);

        return new ProvisioningResult(savedOrganization, defaultStore);
    }

    /**
     * Provision an additional store for an existing organization.
     *
     * @param organization The organization to add the store to
     * @param storeName    Name of the new store
     * @param location     Location of the store
     * @return The created Store
     */
    public Store provisionStore(Organization organization, String storeName, String location) {
        Store store = new Store();
        store.setOrganization(organization);
        store.setName(storeName);
        store.setLocation(location);
        store.setStatus(StoreStatus.ACTIVE);
        Store savedStore = storeRepository.save(store);

        log.info("Created store '{}' for organization '{}'", savedStore.getName(), organization.getName());
        return savedStore;
    }

    /**
     * Assign a role to a user within an organization.
     *
     * @param userId       The user's ID
     * @param organization The organization
     * @param store        Optional store (null for organization-level roles)
     * @param roleType     The role type to assign
     * @return The created UserOrganizationRole (not yet saved - userId may need to be set first)
     */
    public UserOrganizationRole createRoleAssignment(UUID userId, Organization organization, Store store, RoleType roleType) {
        Role role = roleRepository.findByName(roleType.name())
                .orElseThrow(() -> new IllegalStateException(roleType.name() + " role not found in database."));

        return createRoleAssignment(userId, organization, store, role.getId());
    }

    /**
     * Assign a role to a user within an organization using role ID.
     *
     * @param userId       The user's ID (can be null if user hasn't been saved yet)
     * @param organization The organization
     * @param store        Optional store (null for organization-level roles)
     * @param roleId       The role ID to assign
     * @return The created UserOrganizationRole
     */
    public UserOrganizationRole createRoleAssignment(UUID userId, Organization organization, Store store, UUID roleId) {
        UserOrganizationRole userOrgRole = new UserOrganizationRole();
        userOrgRole.setUserId(userId);
        userOrgRole.setOrganization(organization);
        userOrgRole.setStore(store);
        userOrgRole.setRoleId(roleId);

        log.debug("Created role assignment for user {} in organization {} with role {}",
                userId, organization.getId(), roleId);

        return userOrgRole;
    }

    /**
     * Add a user to an existing organization (typically used for invitations).
     *
     * @param userId       The user's ID
     * @param organization The organization to add the user to
     * @param store        Optional store for store-level access
     * @param roleId       The role ID to assign
     * @return The saved UserOrganizationRole
     */
    public UserOrganizationRole addUserToOrganization(UUID userId, Organization organization, Store store, UUID roleId) {
        UserOrganizationRole userOrgRole = createRoleAssignment(userId, organization, store, roleId);
        UserOrganizationRole saved = userOrganizationRoleRepository.save(userOrgRole);

        log.info("Added user {} to organization {} with role {}", userId, organization.getId(), roleId);
        logAudit("ADD_USER_TO_ORGANIZATION", userId,
                "User added to organization: " + organization.getName());

        return saved;
    }

    /**
     * Save a role assignment (used when userId is set after user creation).
     *
     * @param userOrgRole The role assignment to save
     * @return The saved UserOrganizationRole
     */
    public UserOrganizationRole saveRoleAssignment(UserOrganizationRole userOrgRole) {
        return userOrganizationRoleRepository.save(userOrgRole);
    }

    private String generateUniqueOrgName(String baseOrgName) {
        String orgName = baseOrgName;
        int suffix = 1;
        while (organizationRepository.findByName(orgName).isPresent()) {
            orgName = baseOrgName + " " + suffix++;
        }
        return orgName;
    }

    private void applyTemplateIfProvided(String templateCode, Organization organization) {
        if (templateCode != null && !templateCode.trim().isEmpty()
                && !templateCode.equalsIgnoreCase("CUSTOM")) {
            try {
                templateCopyService.applyTemplate(organization, templateCode);
                organization.setAppliedTemplateCode(templateCode);
                organizationRepository.save(organization);
                logAudit("APPLY_TEMPLATE", organization.getId(),
                        "Template '" + templateCode + "' applied during provisioning");
                log.info("Applied template '{}' to organization '{}'", templateCode, organization.getName());
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

    /**
     * Result of provisioning a new organization.
     */
    public record ProvisioningResult(
            Organization organization,
            Store defaultStore
    ) {}
}

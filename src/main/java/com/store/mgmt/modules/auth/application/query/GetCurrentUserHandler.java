package com.store.mgmt.modules.auth.application.query;

import com.store.mgmt.modules.auth.application.dto.AuthUserDTO;
import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handler for GetCurrentUserQuery.
 * Returns the currently authenticated user with their active session context.
 */
@Component
@Transactional(readOnly = true)
public class GetCurrentUserHandler implements QueryHandler<GetCurrentUserQuery, AuthUserDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetCurrentUserHandler.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    public GetCurrentUserHandler(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
    }

    @Override
    public AuthUserDTO handle(GetCurrentUserQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting current user: {}", username);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new SecurityException("User not found: " + username));

        // Extract organization and store from JWT claims
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        String organizationId = null;
        String storeId = null;

        if (details instanceof Map) {
            Map<String, Object> claims = (Map<String, Object>) details;
            organizationId = (String) claims.get("org_id");
            storeId = (String) claims.get("store_id");
        } else {
            throw new IllegalStateException("Invalid JWT claims format in Authentication details");
        }

        log.info("Current user: {}, organizationId: {}, storeId: {}", username, organizationId, storeId);

        UUID orgId = organizationId != null ? UUID.fromString(organizationId) : null;
        UUID storeUuid = storeId != null ? UUID.fromString(storeId) : null;

        if (orgId == null) {
            throw new IllegalStateException("No organization selected.");
        }

        // Validate user has access to the organization
        boolean hasAccess = userOrganizationRoleRepository.existsByUserIdAndOrganizationId(user.getId(), orgId);
        if (!hasAccess) {
            throw new SecurityException("User does not have access to organization: " + orgId);
        }

        // Fetch organization with stores
        Organization organization = organizationRepository.findByIdWithStores(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + orgId));

        OrganizationDTO organizationDTO = OrganizationDTO.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .contactInfo(organization.getContactInfo())
                .appliedTemplateCode(organization.getAppliedTemplateCode())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();

        // Get stores user has access to
        List<StoreDTO> stores = user.getOrganizationRoles().stream()
                .filter(uor -> uor.getOrganization().getId().equals(orgId))
                .map(UserOrganizationRole::getStore)
                .filter(Objects::nonNull)
                .distinct()
                .map(store -> StoreDTO.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .location(store.getLocation())
                        .contactInfo(store.getContactInfo())
                        .status(store.getStatus() != null ? store.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());
        organizationDTO.setStores(stores);

        // Get active store if selected
        StoreDTO activeStoreDTO = null;
        if (storeUuid != null) {
            boolean hasStoreAccess = userOrganizationRoleRepository.existsByUserIdAndStoreId(user.getId(), storeUuid);
            if (!hasStoreAccess) {
                throw new SecurityException("User does not have access to store: " + storeUuid);
            }
            Store store = storeRepository.findById(storeUuid)
                    .orElseThrow(() -> new EntityNotFoundException("Store not found: " + storeUuid));
            activeStoreDTO = StoreDTO.builder()
                    .id(store.getId())
                    .name(store.getName())
                    .location(store.getLocation())
                    .contactInfo(store.getContactInfo())
                    .status(store.getStatus() != null ? store.getStatus().name() : null)
                    .build();
        }

        // Get roles and permissions for the active organization
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        user.getOrganizationRoles().stream()
                .filter(uor -> uor.getOrganization().getId().equals(orgId))
                .forEach(uor -> {
                    roles.add(uor.getRole().getName());
                    uor.getRole().getPermissions().forEach(perm -> permissions.add(perm.getName()));
                });

        return AuthUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .imageUrl(user.getImageUrl())
                .active(user.isActive())
                .activeOrganization(organizationDTO)
                .activeStore(activeStoreDTO)
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

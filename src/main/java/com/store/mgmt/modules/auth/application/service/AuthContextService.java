package com.store.mgmt.modules.auth.application.service;

import com.store.mgmt.modules.auth.domain.model.RefreshToken;
import com.store.mgmt.shared.infrastructure.security.JWTService;
import com.store.mgmt.modules.auth.application.dto.AuthUserDTO;
import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.RoleType;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.auth.domain.repository.RefreshTokenRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shared service for authentication context operations.
 * Consolidates common logic used across auth handlers.
 */
@Service
public class AuthContextService {

    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final JpaRoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    public AuthContextService(
            JWTService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            JpaRoleRepository roleRepository,
            OrganizationRepository organizationRepository
    ) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Determines the active organization context for a user.
     * Priority: SUPER_ADMIN role org > first available org
     */
    public ActiveContext determineActiveContext(User user) {
        List<UserOrganizationRole> orgRoles = userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(user.getId());

        if (orgRoles.isEmpty()) {
            throw new IllegalStateException("User account is not associated with any organization.");
        }

        // Fetch all roles by IDs
        List<UUID> roleIds = orgRoles.stream()
                .map(UserOrganizationRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        List<Role> roles = roleRepository.findByIdsWithPermissions(roleIds);
        Map<UUID, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        // Try to find primary organization (SUPER_ADMIN role)
        UserOrganizationRole activeRole = orgRoles.stream()
                .filter(uor -> {
                    Role role = roleMap.get(uor.getRoleId());
                    return role != null && RoleType.SUPER_ADMIN.toString().equals(role.getName())
                            && uor.getOrganization() != null;
                })
                .findFirst()
                .orElseGet(() -> orgRoles.get(0));

        return buildActiveContext(user, activeRole, roleMap);
    }

    /**
     * Builds active context for a specific organization and optional store.
     */
    public ActiveContext buildContextForOrganization(User user, Organization organization, Store store) {
        List<UserOrganizationRole> orgRoles = userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(user.getId());

        // Fetch all roles by IDs
        List<UUID> roleIds = orgRoles.stream()
                .map(UserOrganizationRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        List<Role> roles = roleRepository.findByIdsWithPermissions(roleIds);
        Map<UUID, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        // Find the user's role in this organization
        UserOrganizationRole activeRole = orgRoles.stream()
                .filter(uor -> uor.getOrganization().getId().equals(organization.getId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException("User not authorized for this organization"));

        // Build authorities from all roles in this organization
        List<GrantedAuthority> authorities = orgRoles.stream()
                .filter(uor -> uor.getOrganization().getId().equals(organization.getId()))
                .flatMap(uor -> {
                    Role role = roleMap.get(uor.getRoleId());
                    if (role == null) return java.util.stream.Stream.empty();
                    List<GrantedAuthority> auths = new ArrayList<>();
                    auths.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                    role.getPermissions().forEach(perm ->
                            auths.add(new SimpleGrantedAuthority(perm.getName())));
                    return auths.stream();
                })
                .distinct()
                .collect(Collectors.toList());

        return new ActiveContext(
                organization.getId(),
                store != null ? store.getId() : null,
                authorities,
                activeRole,
                organization,
                store,
                roleMap
        );
    }

    private ActiveContext buildActiveContext(User user, UserOrganizationRole activeRole, Map<UUID, Role> roleMap) {
        Role role = roleMap.get(activeRole.getRoleId());
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            role.getPermissions().forEach(perm ->
                    authorities.add(new SimpleGrantedAuthority(perm.getName())));
        }

        return new ActiveContext(
                activeRole.getOrganization().getId(),
                activeRole.getStore() != null ? activeRole.getStore().getId() : null,
                authorities,
                activeRole,
                activeRole.getOrganization(),
                activeRole.getStore(),
                roleMap
        );
    }

    /**
     * Generates and stores authentication tokens.
     */
    public TokenPair generateTokens(User user, ActiveContext context) {
        String accessToken = jwtService.generateAccessToken(
                user,
                context.organizationId(),
                context.storeId(),
                context.authorities()
        );
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token
        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setToken(refreshToken);
        token.setExpiryDate(new Date(System.currentTimeMillis() + jwtService.getRefreshTokenExpiration()));
        refreshTokenRepository.save(token);

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * Builds AuthUserDTO from user and active context.
     */
    public AuthUserDTO buildAuthUserDTO(User user, ActiveContext context) {
        OrganizationDTO activeOrgDTO = buildOrganizationDTO(user, context);
        StoreDTO activeStoreDTO = buildStoreDTO(context);

        Set<String> roles = context.authorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());

        Set<String> permissions = context.authorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return AuthUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .imageUrl(user.getImageUrl())
                .active(user.isActive())
                .activeOrganization(activeOrgDTO)
                .activeStore(activeStoreDTO)
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private OrganizationDTO buildOrganizationDTO(User user, ActiveContext context) {
        Organization org = context.organization();
        if (org == null) {
            return null;
        }

        OrganizationDTO dto = OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .description(org.getDescription())
                .contactInfo(org.getContactInfo())
                .appliedTemplateCode(org.getAppliedTemplateCode())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();

        // Get stores user has access to in this organization
        List<UserOrganizationRole> userOrgRoles = userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(user.getId());

        // Check if user has an org-level role (store is null) - means access to all stores
        boolean hasOrgLevelRole = userOrgRoles.stream()
                .filter(uor -> uor.getOrganization().getId().equals(org.getId()))
                .anyMatch(uor -> uor.getStore() == null);

        List<StoreDTO> stores;
        if (hasOrgLevelRole) {
            // User has org-level access, show all stores in the organization
            // Fetch organization with stores to avoid lazy loading issues
            Organization orgWithStores = organizationRepository.findByIdWithStores(org.getId())
                    .orElse(org);
            stores = orgWithStores.getStores() != null
                    ? orgWithStores.getStores().stream()
                            .map(this::mapStoreToDTO)
                            .collect(Collectors.toList())
                    : Collections.emptyList();
        } else {
            // User only has store-level access, show only assigned stores
            stores = userOrgRoles.stream()
                    .filter(uor -> uor.getOrganization().getId().equals(org.getId()))
                    .map(UserOrganizationRole::getStore)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(this::mapStoreToDTO)
                    .collect(Collectors.toList());
        }
        dto.setStores(stores);

        return dto;
    }

    private StoreDTO buildStoreDTO(ActiveContext context) {
        Store store = context.store();
        if (store == null) {
            return null;
        }
        return mapStoreToDTO(store);
    }

    private StoreDTO mapStoreToDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .contactInfo(store.getContactInfo())
                .status(store.getStatus() != null ? store.getStatus().name() : null)
                .build();
    }

    /**
     * Active context record containing organization, store, authorities, and role map.
     */
    public record ActiveContext(
            UUID organizationId,
            UUID storeId,
            List<GrantedAuthority> authorities,
            UserOrganizationRole activeRole,
            Organization organization,
            Store store,
            Map<UUID, Role> roleMap
    ) {}

    /**
     * Token pair record containing access and refresh tokens.
     */
    public record TokenPair(String accessToken, String refreshToken) {}
}

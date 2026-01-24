package com.store.mgmt.modules.auth.application.service;

import com.store.mgmt.auth.model.entity.RefreshToken;
import com.store.mgmt.auth.service.JWTService;
import com.store.mgmt.modules.auth.application.dto.AuthUserDTO;
import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.RefreshTokenRepository;
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

    public AuthContextService(JWTService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Determines the active organization context for a user.
     * Priority: SUPER_ADMIN role org > first available org
     */
    public ActiveContext determineActiveContext(User user) {
        Set<UserOrganizationRole> orgRoles = user.getOrganizationRoles();

        if (orgRoles == null || orgRoles.isEmpty()) {
            throw new IllegalStateException("User account is not associated with any organization.");
        }

        // Try to find primary organization (SUPER_ADMIN role)
        UserOrganizationRole activeRole = orgRoles.stream()
                .filter(uor -> RoleType.SUPER_ADMIN.toString().equals(uor.getRole().getName())
                        && uor.getOrganization() != null)
                .findFirst()
                .orElseGet(() -> orgRoles.iterator().next());

        return buildActiveContext(user, activeRole);
    }

    /**
     * Builds active context for a specific organization and optional store.
     */
    public ActiveContext buildContextForOrganization(User user, Organization organization, Store store) {
        Set<UserOrganizationRole> orgRoles = user.getOrganizationRoles();

        // Find the user's role in this organization
        UserOrganizationRole activeRole = orgRoles.stream()
                .filter(uor -> uor.getOrganization().getId().equals(organization.getId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException("User not authorized for this organization"));

        // Build authorities from all roles in this organization
        List<GrantedAuthority> authorities = orgRoles.stream()
                .filter(uor -> uor.getOrganization().getId().equals(organization.getId()))
                .flatMap(uor -> {
                    List<GrantedAuthority> auths = new ArrayList<>();
                    auths.add(new SimpleGrantedAuthority("ROLE_" + uor.getRole().getName()));
                    uor.getRole().getPermissions().forEach(perm ->
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
                store
        );
    }

    private ActiveContext buildActiveContext(User user, UserOrganizationRole activeRole) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + activeRole.getRole().getName()));
        activeRole.getRole().getPermissions().forEach(perm ->
                authorities.add(new SimpleGrantedAuthority(perm.getName())));

        return new ActiveContext(
                activeRole.getOrganization().getId(),
                activeRole.getStore() != null ? activeRole.getStore().getId() : null,
                authorities,
                activeRole,
                activeRole.getOrganization(),
                activeRole.getStore()
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
        token.setUser(user);
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
        List<StoreDTO> stores = user.getOrganizationRoles().stream()
                .filter(uor -> uor.getOrganization().getId().equals(org.getId()))
                .map(UserOrganizationRole::getStore)
                .filter(Objects::nonNull)
                .distinct()
                .map(this::mapStoreToDTO)
                .collect(Collectors.toList());
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
     * Active context record containing organization, store, and authorities.
     */
    public record ActiveContext(
            UUID organizationId,
            UUID storeId,
            List<GrantedAuthority> authorities,
            UserOrganizationRole activeRole,
            Organization organization,
            Store store
    ) {}

    /**
     * Token pair record containing access and refresh tokens.
     */
    public record TokenPair(String accessToken, String refreshToken) {}
}

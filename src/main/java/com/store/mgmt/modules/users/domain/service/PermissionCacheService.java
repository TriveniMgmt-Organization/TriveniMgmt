package com.store.mgmt.modules.users.domain.service;

import com.store.mgmt.config.CacheConfig;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Permission;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaPermissionRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for caching permission lookups to improve authorization performance.
 *
 * Caches:
 * - All permissions (rarely change)
 * - All roles with permissions (rarely change)
 * - User-specific permissions per organization (evicted on role changes)
 */
@Service
@Transactional(readOnly = true)
public class PermissionCacheService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCacheService.class);

    private final JpaPermissionRepository permissionRepository;
    private final JpaRoleRepository roleRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    public PermissionCacheService(
            JpaPermissionRepository permissionRepository,
            JpaRoleRepository roleRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
    }

    /**
     * Get all permissions from cache or database.
     *
     * @return Set of all permission names
     */
    @Cacheable(value = CacheConfig.PERMISSIONS_CACHE, key = "'all'")
    public Set<String> getAllPermissions() {
        log.debug("Loading all permissions from database (cache miss)");
        return permissionRepository.findAll().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Get all roles with their permissions from cache or database.
     *
     * @return Map of role name to set of permission names
     */
    @Cacheable(value = CacheConfig.ROLES_CACHE, key = "'all'")
    public Map<String, Set<String>> getAllRolesWithPermissions() {
        log.debug("Loading all roles with permissions from database (cache miss)");
        return roleRepository.findAllWithPermissions().stream()
                .collect(Collectors.toMap(
                        Role::getName,
                        role -> role.getPermissions().stream()
                                .map(Permission::getName)
                                .collect(Collectors.toSet())
                ));
    }

    /**
     * Get user's permissions for a specific organization.
     * This is the most frequently called method and benefits most from caching.
     *
     * @param userId The user's ID
     * @param orgId  The organization's ID
     * @return Set of permission names the user has in this organization
     */
    @Cacheable(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString() + ':' + #orgId.toString()")
    public Set<String> getUserPermissions(UUID userId, UUID orgId) {
        log.debug("Loading permissions for user {} in org {} from database (cache miss)", userId, orgId);

        // Get user's roles in this organization
        List<UserOrganizationRole> userOrgRoles = userOrganizationRoleRepository
                .findByUserIdWithOrganizationAndStore(userId)
                .stream()
                .filter(uor -> uor.getOrganization().getId().equals(orgId))
                .collect(Collectors.toList());

        if (userOrgRoles.isEmpty()) {
            return Collections.emptySet();
        }

        // Get role IDs
        List<UUID> roleIds = userOrgRoles.stream()
                .map(UserOrganizationRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        // Load roles with permissions
        List<Role> roles = roleRepository.findByIdsWithPermissions(roleIds);

        // Collect all permissions
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Get user's roles for a specific organization.
     *
     * @param userId The user's ID
     * @param orgId  The organization's ID
     * @return Set of role names the user has in this organization
     */
    @Cacheable(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "'roles:' + #userId.toString() + ':' + #orgId.toString()")
    public Set<String> getUserRoles(UUID userId, UUID orgId) {
        log.debug("Loading roles for user {} in org {} from database (cache miss)", userId, orgId);

        // Get user's roles in this organization
        List<UserOrganizationRole> userOrgRoles = userOrganizationRoleRepository
                .findByUserIdWithOrganizationAndStore(userId)
                .stream()
                .filter(uor -> uor.getOrganization().getId().equals(orgId))
                .collect(Collectors.toList());

        if (userOrgRoles.isEmpty()) {
            return Collections.emptySet();
        }

        // Get role IDs
        List<UUID> roleIds = userOrgRoles.stream()
                .map(UserOrganizationRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        // Load roles
        List<Role> roles = roleRepository.findByIdsWithPermissions(roleIds);

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Check if user has a specific permission in an organization.
     *
     * @param userId     The user's ID
     * @param orgId      The organization's ID
     * @param permission The permission to check
     * @return true if user has the permission
     */
    public boolean hasPermission(UUID userId, UUID orgId, String permission) {
        return getUserPermissions(userId, orgId).contains(permission);
    }

    /**
     * Check if user has any of the specified permissions in an organization.
     *
     * @param userId      The user's ID
     * @param orgId       The organization's ID
     * @param permissions The permissions to check
     * @return true if user has any of the permissions
     */
    public boolean hasAnyPermission(UUID userId, UUID orgId, String... permissions) {
        Set<String> userPermissions = getUserPermissions(userId, orgId);
        return Arrays.stream(permissions).anyMatch(userPermissions::contains);
    }

    /**
     * Check if user has all of the specified permissions in an organization.
     *
     * @param userId      The user's ID
     * @param orgId       The organization's ID
     * @param permissions The permissions to check
     * @return true if user has all of the permissions
     */
    public boolean hasAllPermissions(UUID userId, UUID orgId, String... permissions) {
        Set<String> userPermissions = getUserPermissions(userId, orgId);
        return Arrays.stream(permissions).allMatch(userPermissions::contains);
    }

    /**
     * Evict cached permissions for a specific user and organization.
     * Call this when a user's role assignment changes.
     *
     * @param userId The user's ID
     * @param orgId  The organization's ID
     */
    @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "#userId.toString() + ':' + #orgId.toString()")
    public void evictUserPermissions(UUID userId, UUID orgId) {
        log.info("Evicting cached permissions for user {} in org {}", userId, orgId);
    }

    /**
     * Evict cached user roles for a specific user and organization.
     *
     * @param userId The user's ID
     * @param orgId  The organization's ID
     */
    @CacheEvict(value = CacheConfig.USER_PERMISSIONS_CACHE, key = "'roles:' + #userId.toString() + ':' + #orgId.toString()")
    public void evictUserRoles(UUID userId, UUID orgId) {
        log.info("Evicting cached roles for user {} in org {}", userId, orgId);
    }

    /**
     * Evict all cached data for a user in an organization.
     * Call this when a user's role assignment changes.
     *
     * @param userId The user's ID
     * @param orgId  The organization's ID
     */
    public void evictUserCache(UUID userId, UUID orgId) {
        evictUserPermissions(userId, orgId);
        evictUserRoles(userId, orgId);
    }

    /**
     * Evict all permission caches.
     * Call this when roles or permissions are modified.
     */
    @CacheEvict(value = {CacheConfig.PERMISSIONS_CACHE, CacheConfig.ROLES_CACHE, CacheConfig.USER_PERMISSIONS_CACHE}, allEntries = true)
    public void evictAllCaches() {
        log.info("Evicting all permission caches");
    }
}

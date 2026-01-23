package com.store.mgmt.common.service;

import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.users.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Shared authorization service for checking user roles and permissions.
 * Centralizes authorization logic used across multiple services.
 */
@Service
public class AuthorizationService {

    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    public AuthorizationService(UserOrganizationRoleRepository userOrganizationRoleRepository) {
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
    }

    /**
     * Check if user has a specific role within an organization.
     *
     * @param user           The user to check
     * @param roleName       The role name to check for
     * @param organizationId The organization ID
     * @return true if the user has the role in the organization
     */
    public boolean hasRoleInOrganization(User user, String roleName, UUID organizationId) {
        return userOrganizationRoleRepository.findByUserIdAndOrganizationId(user.getId(), organizationId)
                .stream()
                .anyMatch(uor -> uor.getRole().getName().equals(roleName));
    }

    /**
     * Check if user does NOT have a specific role within an organization.
     *
     * @param user           The user to check
     * @param roleName       The role name to check for
     * @param organizationId The organization ID
     * @return true if the user does NOT have the role in the organization
     */
    public boolean lacksRoleInOrganization(User user, String roleName, UUID organizationId) {
        return !hasRoleInOrganization(user, roleName, organizationId);
    }

    /**
     * Check if user has a specific role across any organization.
     *
     * @param user     The user to check
     * @param roleName The role name to check for
     * @return true if the user has the role in any organization
     */
    public boolean hasRole(User user, String roleName) {
        return user.getOrganizationRoles().stream()
                .anyMatch(uor -> uor.getRole().getName().equals(roleName));
    }

    /**
     * Check if user does NOT have a specific role across any organization.
     *
     * @param user     The user to check
     * @param roleName The role name to check for
     * @return true if the user does NOT have the role
     */
    public boolean lacksRole(User user, String roleName) {
        return !hasRole(user, roleName);
    }

    /**
     * Require user to have a specific role in the organization.
     * Throws SecurityException if authorization fails.
     *
     * @param user           The user to check
     * @param roleName       The required role name
     * @param organizationId The organization ID
     * @param action         Description of the action for error message
     * @throws SecurityException if authorization fails
     */
    public void requireRoleInOrganization(User user, String roleName, UUID organizationId, String action) {
        if (lacksRoleInOrganization(user, roleName, organizationId)) {
            throw new SecurityException("User not authorized to " + action);
        }
    }

    /**
     * Require user to have a specific role (across any organization).
     * Throws SecurityException if authorization fails.
     *
     * @param user     The user to check
     * @param roleName The required role name
     * @param action   Description of the action for error message
     * @throws SecurityException if authorization fails
     */
    public void requireRole(User user, String roleName, String action) {
        if (lacksRole(user, roleName)) {
            throw new SecurityException("User not authorized to " + action);
        }
    }
}

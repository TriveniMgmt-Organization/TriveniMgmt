package com.store.mgmt.config.security;

import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final JpaRoleRepository roleRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            JpaRoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        logger.debug("Attempting to load user by username: {}", username);
        return userRepository.findByEmail(username)
                .map(user -> {
                    logger.debug("User found: {}", user.getEmail());
                    if (!user.isActive()) {
                        logger.warn("User {} is inactive.", username);
                    }
                    try {
                        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
                        return new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPasswordHash(),
                                user.isActive(),
                                true,
                                true,
                                true,
                                authorities);
                    } catch (Exception e) {
                        logger.error("Error building UserDetails for user {}: {}", user.getEmail(), e.getMessage(), e);
                        throw new InternalAuthenticationServiceException("Failed to retrieve user details for " + user.getEmail(), e);
                    }
                })
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        logger.debug("Getting authorities for user: {}", user.getEmail());

        List<UserOrganizationRole> orgRoles = userOrganizationRoleRepository.findByUserId(user.getId());
        if (orgRoles.isEmpty()) {
            logger.warn("User {} has no organization roles assigned.", user.getEmail());
            return Collections.emptyList();
        }

        // Fetch all roles by IDs
        List<UUID> roleIds = orgRoles.stream()
                .map(UserOrganizationRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        List<Role> roles = roleRepository.findByIdsWithPermissions(roleIds);
        Map<UUID, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        return orgRoles.stream()
                .map(userOrgRole -> {
                    Role role = roleMap.get(userOrgRole.getRoleId());
                    if (role == null) {
                        logger.error("Role not found for UserOrganizationRole of user {}. Skipping.", user.getEmail());
                        return null;
                    }
                    String roleName = role.getName();
                    if (roleName == null) {
                        logger.error("Role name is null for UserOrganizationRole of user {}. Skipping.", user.getEmail());
                        return null;
                    }
                    logger.debug("Adding role authority: ROLE_{} for user {}", roleName, user.getEmail());
                    return new SimpleGrantedAuthority("ROLE_" + roleName);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}


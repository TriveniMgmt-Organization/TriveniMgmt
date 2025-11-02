package com.store.mgmt.config.security;

import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // This is important for fetching lazy-loaded collections
    public UserDetails loadUserByUsername(String username) {
        logger.debug("Attempting to load user by username: {}", username);
        return userRepository.findByEmail(username)
                .map(user -> {
                    logger.debug("User found: {}", user.getEmail());
                    if (!user.isActive()) {
                        logger.warn("User {} is inactive.", username);
                        // Consider throwing DisabledException here directly if you want
                        // Spring Security to handle it specifically.
                        // The AuthenticationManager usually checks UserDetails.isEnabled() too.
                    }
                    try {
                        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
                        // logger.debug("Authorities for user {}: {}", user.getEmail(), authorities);
                        return new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPasswordHash(),
                                user.isActive(), // enabled
                                true, // accountNonExpired
                                true, // credentialsNonExpired
                                true, // accountNonLocked
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
        if (user.getOrganizationRoles() == null || user.getOrganizationRoles().isEmpty()) {
            logger.warn("User {} has no organization roles assigned.", user.getEmail());
            return Collections.emptyList(); // Or a default "ROLE_USER" if all users should have it
        }
        return user.getOrganizationRoles().stream()
                .map(userOrgRole -> {
                    if (userOrgRole.getRole() == null) {
                        logger.error("UserOrganizationRole for user {} has a null role. Skipping.", user.getEmail());
                        return null; // Filter this out later
                    }
                    String roleName = userOrgRole.getRole().getName();
                    if (roleName == null) {
                        logger.error("Role name is null for UserOrganizationRole of user {}. Skipping.", user.getEmail());
                        return null; // Filter this out later
                    }
                    logger.debug("Adding role authority: ROLE_{} for user {}", roleName, user.getEmail());
                    return new SimpleGrantedAuthority("ROLE_" + roleName);
                })
                .filter(Objects::nonNull) // Filter out any null authorities
                .collect(Collectors.toList());
    }
}


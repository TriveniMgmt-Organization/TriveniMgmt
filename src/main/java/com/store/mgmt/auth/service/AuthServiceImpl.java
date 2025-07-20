package com.store.mgmt.auth.service;

import com.store.mgmt.auth.model.dto.AuthCredentials;
import com.store.mgmt.auth.model.dto.AuthResponse;
import com.store.mgmt.auth.model.dto.RegisterCredentials;
import com.store.mgmt.auth.model.entity.RefreshToken;
import com.store.mgmt.config.TenantContext;
import com.store.mgmt.organization.mapper.OrganizationMapper;
import com.store.mgmt.organization.model.dto.*;
import com.store.mgmt.organization.model.entity.Invitation;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.InvitationRepository;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.users.mapper.UserMapper;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.RefreshTokenRepository;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.service.AuditLogService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

//    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository; // New repository for refresh tokens
    private final AuthenticationManager authenticationManager;
    private final InvitationRepository invitationRepository;
    private final AuditLogService auditLogService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final StoreRepository storeRepository;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, JWTService jwtService,
                            AuthenticationManager authenticationManager,
                            InvitationRepository invitationRepository, StoreRepository storeRepository,
                            OrganizationRepository organizationRepository,
                            AuditLogService auditLogService,
                           OrganizationMapper organizationMapper,
                           UserMapper userMapper, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.storeRepository = storeRepository;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.invitationRepository = invitationRepository;
        this.organizationRepository = organizationRepository;
        this.auditLogService = auditLogService;
    }

    private AuthResponse handleJWTGeneration(User user){
        UUID activeOrganizationId = null;
        UUID activeStoreId = null; // Will be null if the active role is organization-level
        List<GrantedAuthority> authoritiesForActiveOrg = new ArrayList<>();

        // 2. Determine the Active Organization for this Login Session

        // A. Attempt to find the user's "primary" organization (the one they created/own)
        Optional<UserOrganizationRole> primaryOrgRoleOpt = user.getOrganizationRoles().stream()
                .filter(uor -> uor.getRole().getName().equals(RoleType.SUPER_ADMIN.toString()) // Or RoleType.ORGANIZATION_OWNER.toString()
                        && uor.getOrganization() != null) // Ensure organization is not null
                .findFirst();

        if (primaryOrgRoleOpt.isPresent()) {
            // User has a primary organization they own/created -> Default to this one
            UserOrganizationRole primaryRole = primaryOrgRoleOpt.get();
            activeOrganizationId = primaryRole.getOrganization().getId();

            // Populate authorities for the primary role
            authoritiesForActiveOrg.add(new SimpleGrantedAuthority("ROLE_" + primaryRole.getRole().getName()));
            primaryRole.getRole().getPermissions().forEach(perm -> authoritiesForActiveOrg.add(new SimpleGrantedAuthority(perm.getName())));
            log.info("User {} defaulted to primary organization: {}", user.getEmail(), activeOrganizationId);

            // Set activeStoreId if the primary role is inherently tied to a specific store
            activeStoreId = primaryRole.getStore() != null ? primaryRole.getStore().getId() : null;

        } else if (!user.getOrganizationRoles().isEmpty()) {
            // User does not own an organization but is invited to one or more.
            // If only one invited org, default to that.
            // If multiple invited orgs, you might need a different response / frontend selection.

            if (user.getOrganizationRoles().size() == 1) {
                // Only one organization they belong to (invited) -> Default to that one
                UserOrganizationRole singleRole = user.getOrganizationRoles().iterator().next(); // Safe if size is 1
                activeOrganizationId = singleRole.getOrganization().getId();

                // Populate authorities for this single invited role
                authoritiesForActiveOrg.add(new SimpleGrantedAuthority("ROLE_" + singleRole.getRole().getName()));
                singleRole.getRole().getPermissions().forEach(perm -> authoritiesForActiveOrg.add(new SimpleGrantedAuthority(perm.getName())));
                log.info("User {} defaulted to single invited organization: {}", user.getEmail(), activeOrganizationId);

                activeStoreId = singleRole.getStore() != null ? singleRole.getStore().getId() : null;

            } else {
                // User has multiple invited organizations.
                // You have a design choice here:
                // 1. Force Selection (Recommended for clarity): Return a custom AuthResponse
                //    that includes a list of organizations and a flag indicating selection is needed.
                //    No access token is generated yet. User makes selection, then another endpoint
                //    (`/auth/select-organization`) generates the token.
                // 2. Default to one (Less Ideal UX for multiple invites): Pick the "first"
                //    available. This is generally not recommended as it can be confusing.
                //    If you *must* default, consider a predictable heuristic (e.g., alphabetically by org name).
                //    For now, let's proceed with a default, but acknowledge this as a UX improvement area.

                // For demonstration, let's pick the first one, but add a warning/TODO
                log.warn("User {} has multiple organization roles (invited), defaulting to the first one. Consider implementing explicit organization selection for better UX.", user.getEmail());
                UserOrganizationRole firstAvailableRole = user.getOrganizationRoles().iterator().next(); // Still non-deterministic if not filtered
                activeOrganizationId = firstAvailableRole.getOrganization().getId();
                authoritiesForActiveOrg.add(new SimpleGrantedAuthority("ROLE_" + firstAvailableRole.getRole().getName()));
                firstAvailableRole.getRole().getPermissions().forEach(perm -> authoritiesForActiveOrg.add(new SimpleGrantedAuthority(perm.getName())));
                activeStoreId = firstAvailableRole.getStore() != null ? firstAvailableRole.getStore().getId() : null;
            }

        } else {
            // This scenario should ideally not happen if user successfully authenticated
            // and `user.isActive()` is true, unless they have no UserOrganizationRole entries at all.
            log.error("Authenticated user {} has no associated organization roles. Please check data integrity.", user.getEmail());
            throw new IllegalStateException("User account is not associated with any organization.");
        }

        // Ensure we always have an activeOrganizationId if we reached this point successfully
        if (activeOrganizationId == null) {
            log.error("Failed to determine active organization ID for user: {}", user.getEmail());
            throw new IllegalStateException("Could not determine active organization for user session.");
        }

        // 3. Generate JWT with the Determined Active Context
        // userDetails should ideally be populated with the roles specific to activeOrganizationId
        // but since we're passing authoritiesForActiveOrg directly, it's fine if userDetails has general roles.
//        UserDetails userDetails = createUserDetails(user); // Ensure this creates UserDetails correctly

        AuthToken aT = generateAndStoreAuthToken(user, activeOrganizationId, activeStoreId, authoritiesForActiveOrg);
//        String accessToken = jwtService.generateAccessToken(user, activeOrganizationId, activeStoreId, authoritiesForActiveOrg);
//        String refreshToken = jwtService.generateRefreshToken(user);

        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Generated tokens for user: " + user.getUsername() +
                "\nAccess Token: " + aT.getAccessToken() +
                "\nRefresh Token: " + aT.getRefreshToken());


//        storeRefreshToken(user, refreshToken);
        return new AuthResponse(aT.getAccessToken(), aT.getRefreshToken(), userMapper.toDto(user));
    }

    @Override
    @Transactional
    public AuthResponse authenticateUser(AuthCredentials credentials) {
        log.info("Authenticating user: {}", credentials.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword())
            );

            User user = userRepository.findByUsername(credentials.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid username"));

            if (!user.isActive()) {
                log.warn("User account inactive: {}", credentials.getUsername());
                throw new DisabledException("User account is inactive");
            }

            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Authentication success creating tokens for user: " + user.getUsername());
            return handleJWTGeneration(user);
//            UserDetails userDetails = createUserDetails(user);
//            String accessToken = jwtService.generateAccessToken(userDetails, user);
//            String refreshToken = jwtService.generateRefreshToken(userDetails, user);

//            storeRefreshToken(user, refreshToken);

//            UserDTO userDTO = userMapper.toDto(user);
//            return new AuthResponse(accessToken, refreshToken, userDTO);
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", credentials.getUsername());
            throw e;
        } catch (DisabledException e) {
            log.warn("Account disabled for user: {}", credentials.getUsername());
            throw e;
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", credentials.getUsername(), e);
            throw new BadCredentialsException("Authentication failed", e);
        }
    }

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterCredentials registrationData) {
        log.info("Registering user: {}", registrationData.getEmail());

        if (userRepository.findByEmail(registrationData.getEmail()).isPresent()) {
            log.warn("Email already registered: {}", registrationData.getEmail());
            throw new IllegalArgumentException("Email '" + registrationData.getEmail() + "' is already registered.");
        }
        User user = new User();
        user.setFirstName(registrationData.getFirstName());
        user.setLastName(registrationData.getLastName());
        user.setUsername(registrationData.getEmail());
        user.setEmail(registrationData.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationData.getPassword()));
        user.setActive(true);

        UUID activeOrganizationId = null;
        UUID activeStoreId = null;
        if (registrationData.getInvitationToken() != null) {
            Invitation invitation = invitationRepository.findByTokenAndUsedFalse(registrationData.getInvitationToken())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invitation token."));
            if (!invitation.getEmail().equals(registrationData.getEmail())) {
                throw new IllegalArgumentException("Invitation token does not match email.");
            }
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Invitation token has expired.");
            }

            activeOrganizationId = invitation.getOrganization().getId();
            activeStoreId = invitation.getStore() != null ? invitation.getStore().getId() : null;
            UserOrganizationRole userOrgRole = new UserOrganizationRole();
            userOrgRole.setUser(user);
            userOrgRole.setOrganization(invitation.getOrganization());
            userOrgRole.setRole(invitation.getRole());
            userOrgRole.setStore(invitation.getStore());
            user.setOrganizationRoles(Set.of(userOrgRole));

            invitation.setUsed(true);
            invitationRepository.save(invitation);
        } else {
            String orgName = registrationData.getFirstName() + "'s Organization";
            int suffix = 1;
            String baseOrgName = orgName;
            while (organizationRepository.findByName(orgName).isPresent()) {
                orgName = baseOrgName + " " + suffix++;
            }

            Organization organization = new Organization();
            organization.setName(orgName);
            Organization savedOrganization = organizationRepository.save(organization);

            activeOrganizationId = savedOrganization.getId();
            // New user without invitation becomes SUPER_ADMIN
            Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN.toString())
                    .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found."));
            UserOrganizationRole userOrgRole = new UserOrganizationRole();
            userOrgRole.setUser(user);
            userOrgRole.setOrganization(savedOrganization);
            userOrgRole.setRole(superAdminRole);
            user.setOrganizationRoles(Set.of(userOrgRole));
        }

        User savedUser = userRepository.save(user);
//        Role userRole = roleRepository.findByName("CUSTOMER")
//                .orElseThrow(() -> new IllegalStateException("Default 'CUSTOMER' role not found."));
//        newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        UUID finalActiveOrganizationId = activeOrganizationId;
        List<GrantedAuthority> authoritiesForToken = savedUser.getOrganizationRoles().stream()
                .filter(uor -> uor.getOrganization().getId().equals(finalActiveOrganizationId))
                .map(uor -> new SimpleGrantedAuthority(uor.getRole().getName()))
                .collect(Collectors.toList());
        AuthToken aT = generateAndStoreAuthToken(savedUser, activeOrganizationId, activeStoreId, authoritiesForToken);
        auditLogService.log("REGISTER_USER", savedUser.getId(), "User registered: " + savedUser.getEmail());
        return new AuthResponse(aT.getAccessToken(), aT.getRefreshToken(), userMapper.toDto(user));
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        UserDetails userDetails = loadUserByRefreshToken(refreshToken);
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Invalidate old refresh token
        refreshTokenRepository.deleteByToken(refreshToken);

        AuthToken aT = generateAndStoreAuthToken(user, null, null, Collections.emptyList());
        return new AuthResponse(aT.getAccessToken(), aT.getRefreshToken(), userMapper.toDto(user));
    }
    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logging out user");
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Override
    public AuthResponse validateToken(String token) {
        JWTService.JwtData jwtData = jwtService.extractJwtData(token);
        String email = jwtData.username;
        UUID orgId = jwtData.organizationId;
        UUID storeId = jwtData.storeId;
        log.info("Validating token for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (jwtService.validateToken(token, user)) {
            UserDTO userDTO = userMapper.toDto(user);
            return new AuthResponse(token, null, userDTO);
        } else {
            log.warn("Invalid or expired token for user: {}", email);
            throw new JwtException("Invalid or expired token");
        }
    }


    private UserDetails createUserDetails(User user) {
        // Include both roles (with ROLE_ prefix) and permissions (without prefix)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(user.getOrganizationRoles().stream()
                .map(orole -> new SimpleGrantedAuthority("ROLE_" + orole.getRole().getName()))
                .toList());
        authorities.addAll(user.getOrganizationRoles().stream()
                .flatMap(orole -> orole.getRole().getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrganizationDTO> getOrganizations() {
        log.info("Retrieving organizations for user ID: {}", TenantContext.getCurrentUser().getId());
        User user = TenantContext.getCurrentUser();
        return user.getOrganizationRoles().stream()
                .map(UserOrganizationRole::getOrganization)
                .map(organizationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuthResponse selectTenant(TenantDTO selectDTO) {
        log.info("Selecting tenant with organization ID: {} and store ID: {}",
                selectDTO.getOrganizationId(), selectDTO.getStoreId());

        User user = TenantContext.getCurrentUser();
        boolean hasAccess = user.getOrganizationRoles().stream()
                .anyMatch(uor -> uor.getOrganization().getId().equals(selectDTO.getOrganizationId()) &&
                        (selectDTO.getStoreId() == null ||
                                (uor.getStore() != null && uor.getStore().getId().equals(selectDTO.getStoreId()))));

        if (!hasAccess) {
            throw new SecurityException("User not authorized for this organization or store.");
        }

        Organization organization = organizationRepository.findById(selectDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));

        Store store = null;
        if (selectDTO.getStoreId() != null) {
            store = storeRepository.findById(selectDTO.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("Store not found."));
            if (!store.getOrganization().getId().equals(selectDTO.getOrganizationId())) {
                throw new IllegalArgumentException("Store does not belong to the specified organization.");
            }
        }
        List<GrantedAuthority> authoritiesForToken = user.getOrganizationRoles().stream()
                .filter(uor -> uor.getOrganization().getId().equals(organization.getId()))
                .map(uor -> new SimpleGrantedAuthority(uor.getRole().getName()))
                .collect(Collectors.toList());
        auditLogService.log("SELECT_TENANT", user.getId(),
                "Selected organization ID: " + selectDTO.getOrganizationId() +
                        (selectDTO.getStoreId() != null ? ", store ID: " + selectDTO.getStoreId() : ""));

        AuthToken aT = generateAndStoreAuthToken(user,
                organization != null ? organization.getId() : null,
                store != null ? store.getId() : null,
                authoritiesForToken);
        return new AuthResponse(aT.getAccessToken(), aT.getRefreshToken(), userMapper.toDto(user));
    }


    private void storeRefreshToken(User user, String refreshToken) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(refreshToken);
        token.setExpiryDate(new Date(System.currentTimeMillis() + jwtService.getRefreshTokenExpiration()));
        refreshTokenRepository.save(token);
    }

    private UserDetails loadUserByRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new JwtException("Invalid refresh token"));
        if (token.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(token);
            throw new JwtException("Refresh token expired");
        }
        User user = token.getUser();
        return createUserDetails(user);
    }


    @Data
    @AllArgsConstructor
    private static class AuthToken {
        private String accessToken;
        private String refreshToken;
    }
    private AuthToken generateAndStoreAuthToken(User user, UUID activeOrganizationId, UUID activeStoreId, List<GrantedAuthority> authoritiesForActiveOrg) {
        String accessToken = jwtService.generateAccessToken(user, activeOrganizationId, activeStoreId, authoritiesForActiveOrg );
        String refreshToken = jwtService.generateRefreshToken(user);

        storeRefreshToken(user, refreshToken);

        return new AuthToken(accessToken, refreshToken);
    }

}

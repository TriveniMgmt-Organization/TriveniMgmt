package com.store.mgmt.users.service;

import com.store.mgmt.organization.mapper.OrganizationMapper;
import com.store.mgmt.organization.mapper.StoreMapper;
import com.store.mgmt.organization.model.dto.*;
import com.store.mgmt.organization.model.entity.Invitation;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.InvitationRepository;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.users.mapper.UserMapper;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.dto.*;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final OrganizationMapper organizationMapper;
    private final InvitationRepository invitationRepository;
    private final StoreMapper storeMapper;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           OrganizationRepository organizationRepository, StoreRepository storeRepository,
                           UserOrganizationRoleRepository userOrganizationRoleRepository,
                           OrganizationMapper organizationMapper, StoreMapper storeMapper,
                           EmailService emailService, AuditLogService auditLogService,
                           InvitationRepository invitationRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.storeMapper = storeMapper;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.invitationRepository = invitationRepository;
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO dto) {
        if (dto.getUsername() == null || dto.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and email are required");
        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getEmail());
        user.setEmail(dto.getEmail());
        user.setActive(true);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public UserDTO getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UpdateUserDTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (dto.getUsername() != null && !dto.getUsername().equals(existingUser.getUsername()) &&
                userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(existingUser.getEmail()) &&
                userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }


        userMapper.updateEntityFromDto(dto, existingUser);
        if (dto.getEmail() != null && !dto.getEmail().equals(existingUser.getEmail())){
            existingUser.setUsername(dto.getEmail());
        }
        User updated = userRepository.save(existingUser);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        userRepository.save(user);
    }
    @Override
    @Transactional
    public UserDTO assignRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
//        user.getRoles().add(role);
        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional
    public UserDTO removeRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//        UserOrganizationRole role = .findById(roleId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
//        user.getOrganizationRoles().remove(role);
        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    private boolean hasRoleInOrganization(User user, String roleName, UUID organizationId) {
        return userOrganizationRoleRepository.findByUserIdAndOrganizationId(user.getId(), organizationId)
                .stream()
                .anyMatch(uor -> uor.getRole().getName().equals(roleName));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getOrganizationRoles().stream()
                .anyMatch(uor -> uor.getRole().getName().equals(roleName));
    }
    @Transactional
//    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    public void inviteUser(InviteUserDTO inviteDTO) {
        log.info("Inviting user with email: {} to organization ID: {}", inviteDTO.getEmail(), inviteDTO.getOrganizationId());

        Organization organization = organizationRepository.findById(inviteDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));
        Role role = roleRepository.findByName(inviteDTO.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + inviteDTO.getRoleName()));
        Store store = null;
        if (inviteDTO.getStoreId() != null) {
            store = storeRepository.findById(inviteDTO.getStoreId())
                    .orElseThrow(() -> new IllegalArgumentException("Store not found."));
            if (!store.getOrganization().getId().equals(inviteDTO.getOrganizationId())) {
                throw new IllegalArgumentException("Store does not belong to the specified organization.");
            }
        }

        // Check if user already exists in organization
        Optional<User> existingUser = userRepository.findByEmail(inviteDTO.getEmail());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            boolean hasRoleInOrg = userOrganizationRoleRepository.findByUserIdAndOrganizationId(user.getId(), inviteDTO.getOrganizationId())
                    .stream()
                    .anyMatch(uor -> uor.getRole().getName().equals(inviteDTO.getRoleName()) &&
                            (uor.getStore() == null || uor.getStore().getId().equals(inviteDTO.getStoreId())));
            if (hasRoleInOrg) {
                throw new IllegalArgumentException("User already has the specified role in the organization.");
            }

            UserOrganizationRole userOrgRole = new UserOrganizationRole();
            userOrgRole.setUser(user);
            userOrgRole.setOrganization(organization);
            userOrgRole.setRole(role);
            userOrgRole.setStore(store);
            userOrganizationRoleRepository.save(userOrgRole);

            logAuditEntry("ADD_USER_TO_ORG", user.getId(), "Added user to organization ID: " + organization.getId() + " with role: " + role.getName());
        } else {
            String token = UUID.randomUUID().toString();
            Invitation invitation = new Invitation();
            invitation.setEmail(inviteDTO.getEmail());
            invitation.setToken(token);
            invitation.setOrganization(organization);
            invitation.setRole(role);
            invitation.setStore(store);
            invitation.setCreatedAt(LocalDateTime.now());
            invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
            invitation.setUsed(false);
            invitationRepository.save(invitation);

            emailService.sendInvitationEmail(inviteDTO.getEmail(), token, organization.getName(), role.getName());
            logAuditEntry("INVITE_USER", invitation.getId(), "Invited user: " + inviteDTO.getEmail() + " to organization ID: " + organization.getId());
        }
    }

    @Override
    @Transactional
    public void assignUserToOrganization(CreateUserAssignmentDTO dto) {
        log.info("Assigning user ID: {} to organization ID: {} with role: {}", dto.getUserId(), dto.getOrganizationId(), dto.getRoleId());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found."));
        Organization organization = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        if (userOrganizationRoleRepository.findByUserIdAndOrganizationId(dto.getUserId(), dto.getOrganizationId()).isPresent()) {
            throw new IllegalArgumentException("User already assigned to this organization.");
        }

// Check if current user is authorized assign users to organization. SUPER_ADMIN are only allowed
        if(!hasRole(currentUser, RoleType.SUPER_ADMIN.toString())) {
            throw new SecurityException("User not authorized to assign users in this organization.");
        }

        User assignee = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found" ));
        UserOrganizationRole assignment = new UserOrganizationRole();
        assignment.setUser(assignee);
        assignment.setOrganization(organization);
        assignment.setRole(role);
        userOrganizationRoleRepository.save(assignment);
    }

    @Override
    @Transactional
    public void assignUserToStore(CreateUserAssignmentDTO dto) {
        log.info("Assigning user ID: {} to store ID: {} with role: {}", dto.getUserId(), dto.getStoreId(), dto.getRoleId());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found."));
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Check if current user is authorized for the organization
        if (!hasRoleInOrganization(currentUser, RoleType.ORG_ADMIN.toString(), store.getOrganization().getId()) &&
                !hasRole(currentUser, RoleType.SUPER_ADMIN.toString())) {
            throw new SecurityException("User not authorized to assign users in this organization.");
        }

        User assignee = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found" ));

        // Check if the assignee already has the role for the store
        boolean hasRole = userOrganizationRoleRepository.findByUserIdAndOrganizationId(assignee.getId(), store.getOrganization().getId())
                .stream()
                .anyMatch(uor -> uor.getRole().getId().equals(dto.getRoleId()) &&
                        (uor.getStore() == null || uor.getStore().getId().equals(dto.getStoreId())));

        if (hasRole) {
            throw new IllegalArgumentException("User already has the role " + role.getName() + " for the store or organization.");
        }

        UserOrganizationRole assignment = new UserOrganizationRole();
        assignment.setUser(assignee);
        assignment.setOrganization(store.getOrganization());
        assignment.setStore(store);
        assignment.setRole(role);
        UserOrganizationRole savedAssignment = userOrganizationRoleRepository.save(assignment);

        logAuditEntry("ASSIGN_USER_TO_STORE", assignee.getId(),
                "Assigned user to store ID: " + store.getName() + " with role: " + role.getName());
    }


    private void logAuditEntry(String action, UUID entityId, String message) {
        try {
            System.out.println("Audit entry logged successfully: " + log);
            auditLogService.builder()
                    .action(action)
//                    .entityType("Store")
                    .entityId(entityId)
                    .message(message)
                    .log();
        } catch (Exception e) {
            throw new RuntimeException("Failed to log audit entry", e);
        }
    }
}
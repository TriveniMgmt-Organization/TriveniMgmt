package com.store.mgmt.organization.service;

import com.store.mgmt.organization.mapper.OrganizationMapper;
import com.store.mgmt.organization.model.dto.CreateOrganizationDTO;
import com.store.mgmt.organization.model.dto.OrganizationDTO;
import com.store.mgmt.organization.model.dto.UpdateOrganizationDTO;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.service.AuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;

    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationMapper organizationMapper;
    private final AuditLogService auditLogService;

    public OrganizationServiceImpl(OrganizationMapper organizationMapper,
                                   OrganizationRepository organizationRepository,
                                   UserOrganizationRoleRepository userOrganizationRoleRepository,
                                   AuditLogService auditLogService,
                                      RoleRepository roleRepository,
                                   UserRepository userRepository) {
        this.organizationMapper = organizationMapper;
        this.organizationRepository = organizationRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public OrganizationDTO createOrganization(CreateOrganizationDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization Name is required");
        }
        if (organizationRepository.findByName(dto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organization with name '" + dto.getName() + "' already exists"); // Use CONFLICT for existing resource
        }

        // Get current authenticated user's username (email)
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUserName) // Assuming username is email
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + currentUserName));

        // 1. Create and Save Organization
        Organization organization = organizationMapper.toEntity(dto);
        organization.setCreatedBy(currentUserName); // Set creator
        organization.setCreatedAt(LocalDateTime.now()); // Set creation timestamp (if not handled by JPA Auditing)
        // Set any other default values for Organization if needed, e.g., status

        Organization savedOrganization = organizationRepository.save(organization); // Save the organization to get its ID

        // 2. Assign SUPER_ADMIN role to the creator in this new organization
        UserOrganizationRole userOrgRoleAssignment = new UserOrganizationRole();
        userOrgRoleAssignment.setUser(currentUser);
        userOrgRoleAssignment.setOrganization(savedOrganization); // Link to the newly saved organization

        // Fetch the SUPER_ADMIN role from the database
        Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found in database. Please ensure roles are seeded."));
        userOrgRoleAssignment.setRole(superAdminRole);

        userOrgRoleAssignment.setCreatedAt(LocalDateTime.now()); // Set creation timestamp for the assignment
        userOrgRoleAssignment.setCreatedBy(currentUserName); // Creator of the assignment

        userOrganizationRoleRepository.save(userOrgRoleAssignment); // Save the role assignment

        // 3. Log in AuditLog
        auditLogService.log("CREATE_ORGANIZATION", savedOrganization.getId(),
                "Organization '" + savedOrganization.getName() + "' created by user: " + currentUserName);

        // 4. Return DTO of the fully persisted organization
        return organizationMapper.toDto(savedOrganization);
    }

    @Override
    @Transactional
    public OrganizationDTO updateOrganization(UUID id, UpdateOrganizationDTO request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

         organizationMapper.updateEntityFromDto(request, organization);

        Organization updatedOrganization = organizationRepository.save(organization);

        // Log in AuditLog
        auditLogService.log("UPDATE_ORGANIZATION", updatedOrganization.getId(), "Updated organization: " + updatedOrganization.getName());

        return organizationMapper.toDto(updatedOrganization);
    }

    @Override
    public void deleteOrganization(UUID id) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        // Check if there are any user assignments for this organization
        if (userOrganizationRoleRepository.findByOrganizationId(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete organization with existing user assignments");
        }

        organizationRepository.delete(organization);

        // Log in AuditLog
        auditLogService.log("DELETE_ORGANIZATION", id, "Deleted organization: " + organization.getName());
    }
}
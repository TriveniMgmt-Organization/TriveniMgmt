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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUserName) // Assuming username is email
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + currentUserName));

        Organization organization = organizationMapper.toEntity(dto);
        Organization savedOrganization = organizationRepository.save(organization); // Save the organization to get its ID

        UserOrganizationRole userOrgRole = new UserOrganizationRole();
        userOrgRole.setUser(currentUser);
        userOrgRole.setOrganization(savedOrganization); // Link to the newly saved organization

        Role adminRole = roleRepository.findByName(RoleType.ORG_ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("ORG_ADMIN role not found in database. Please ensure roles are seeded."));
        userOrgRole.setRole(adminRole);
        userOrganizationRoleRepository.save(userOrgRole); // Save the role assignment

        logAuditEntry("CREATE_ORGANIZATION", savedOrganization.getId(),
                "Organization '" + savedOrganization.getName() + "' created by user: " + currentUserName);

        return organizationMapper.toDto(savedOrganization);
    }

    @Override
    @Transactional
    public OrganizationDTO updateOrganization(UUID id, UpdateOrganizationDTO dto) {
        System.out.println("Updateing Organization for id: " + id  );
        System.out.println("Request to update Organization: " + dto.getName() + "///"+ dto.getDescription());
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        System.out.println("Found Organization: " + organization.getName() + " for id: " + id);
         organizationMapper.updateEntityFromDto(dto, organization);

        System.out.println("Updating Organization: " + organization.getName() + " with new details: " + dto);

        Organization updatedOrganization = organizationRepository.save(organization);

        System.out.println("Updated Organization: " + updatedOrganization.getName() + " with id: " + updatedOrganization.getId());

        logAuditEntry("UPDATE_ORGANIZATION", updatedOrganization.getId(), "Updated organization: " + updatedOrganization.getName());
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
        logAuditEntry("DELETE_ORGANIZATION", id, "Deleted organization: " + organization.getName());
    }

    private void logAuditEntry(String action, UUID entityId, String message) {
        try {
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
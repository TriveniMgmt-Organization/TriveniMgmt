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
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.service.AuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;

    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    private final UserRepository userRepository;
    private final OrganizationMapper organizationMapper;
    private final AuditLogService auditLogService;

    public OrganizationServiceImpl(OrganizationMapper organizationMapper,
                                   OrganizationRepository organizationRepository,
                                   UserOrganizationRoleRepository userOrganizationRoleRepository,
                                   AuditLogService auditLogService,
                                   UserRepository userRepository) {
        this.organizationMapper = organizationMapper;
        this.organizationRepository = organizationRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public OrganizationDTO createOrganization(CreateOrganizationDTO dto) {
        if (dto.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization Name is required");
        }
        if (organizationRepository.findByName(dto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization already exists");
        }

        Organization organization = organizationMapper.toEntity(dto);

        Organization saved = organizationRepository.save(organization);

        User user = userRepository.findById(dto.getInitialAdminId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserOrganizationRole assignment = new UserOrganizationRole();
        assignment.setUser(user);
        assignment.setOrganization(organization);
        Role role = new Role();
        role.setName(RoleType.SUPER_ADMIN.name());
        assignment.setRole(role);
        userOrganizationRoleRepository.save(assignment);

        // Log in AuditLog
        auditLogService.log("CREATE_ORGANIZATION", organization.getId(), "Created organization: " + organization.getName());

        return organizationMapper.toDto(organization);
    }

    @Override
    public OrganizationDTO updateOrganization(UpdateOrganizationDTO request) {
        Organization organization = organizationRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        if (request.getName() != null) {
            organization.setName(request.getName());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getContactInfo() != null) {
            organization.setContactInfo(request.getContactInfo());
        }

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
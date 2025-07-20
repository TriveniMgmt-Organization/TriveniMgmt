package com.store.mgmt.organization.service;

import com.store.mgmt.config.TenantContext;
import com.store.mgmt.organization.model.dto.CreateUserAssignmentDTO;
import com.store.mgmt.organization.model.dto.UpdateUserAssignmentDTO;
import com.store.mgmt.organization.model.dto.UserAssignmentDTO;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.organization.model.entity.UserAssignment;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.organization.repository.UserAssignmentRepository;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UserAssignmentServiceImpl implements UserAssignmentService {
    private UserOrganizationRoleRepository userOrganizationRoleRepository;

    private UserRepository userRepository;

    private StoreRepository storeRepository;

    private RoleRepository roleRepository;
    private AuditLogService auditLogService;

    @Override
    public UserAssignmentDTO createUserAssignment(CreateUserAssignmentDTO dto) {
        return null;
    }

    @Override
    public UserAssignmentDTO getUserAssignmentById(String id) {
        return null;
    }

    @Override
    public UserAssignmentDTO updateUserAssignment(String id, UpdateUserAssignmentDTO dto) {
        return null;
    }
}
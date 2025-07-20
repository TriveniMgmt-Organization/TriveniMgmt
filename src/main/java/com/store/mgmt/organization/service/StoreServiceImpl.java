package com.store.mgmt.organization.service;

import com.store.mgmt.config.TenantContext;
import com.store.mgmt.organization.mapper.StoreMapper;
import com.store.mgmt.organization.model.dto.CreateStoreDTO;
import com.store.mgmt.organization.model.dto.StoreDTO;
import com.store.mgmt.organization.model.dto.UpdateStoreDTO;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.organization.model.entity.UserAssignment;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.mapping.UserDefinedObjectType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
    public class StoreServiceImpl implements StoreService {
        private final StoreRepository storeRepository;

        private final UserOrganizationRoleRepository userOrganizationRoleRepository;

        private final UserRepository userRepository;
    private final RoleRepository roleRepository;
        private final AuditLogService auditLogService;
        private final OrganizationRepository organizationRepository;
        private final StoreMapper storeMapper;
        public StoreServiceImpl(StoreRepository storeRepository, OrganizationRepository organizationRepository, UserOrganizationRoleRepository userOrganizationRoleRepository, UserRepository userRepository,
                                RoleRepository roleRepository,
                                AuditLogService auditLogService, StoreMapper storeMapper  ) {
            this.organizationRepository = organizationRepository;
            this.storeRepository = storeRepository;
            this.userOrganizationRoleRepository = userOrganizationRoleRepository;
            this.userRepository = userRepository;
            this.roleRepository = roleRepository;
            this.auditLogService = auditLogService;
            this.storeMapper = storeMapper;
        }

    @Override
    @Transactional
    public StoreDTO createStore(CreateStoreDTO createDTO) {
        log.info("Creating store: {} for organization ID: {}", createDTO.getName(), createDTO.getOrganizationId());

        // Fetch the organization from the provided organizationId
        Organization organization = organizationRepository.findById(createDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));
        if (storeRepository.findByNameAndOrganizationId(createDTO.getName(), createDTO.getOrganizationId()).isPresent()) {
            throw new IllegalArgumentException("Store name '" + createDTO.getName() + "' already exists in organization.");
        }

        User currentUser = TenantContext.getCurrentUser();
        if (!hasRoleInOrganization(currentUser, RoleType.ADMIN.toString(), createDTO.getOrganizationId()) &&
                !hasRole(currentUser, RoleType.SUPER_ADMIN.toString())) {
            throw new SecurityException("User not authorized to create stores in this organization.");
        }

        Store store = storeMapper.toEntity(createDTO);
        store.setOrganization(organization);
        Store savedStore = storeRepository.save(store);

        auditLogService.log("CREATE_STORE", savedStore.getId(), "Created store: " + savedStore.getName() + " in organization ID: " + organization.getId());
        return storeMapper.toDto(savedStore);
    }

    @Override
    @Transactional
    public StoreDTO updateStore(UUID id, UpdateStoreDTO dto) {
        log.info("Updating store with ID: {}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found."));

        User currentUser = TenantContext.getCurrentUser();
        if (!hasRoleInOrganization(currentUser, RoleType.ADMIN.toString(), store.getOrganization().getId()) &&
                !hasRole(currentUser, RoleType.SUPER_ADMIN.toString())) {
            throw new SecurityException("User not authorized to update stores in this organization.");
        }

        // Update the store details
        storeMapper.updateEntityFromDto(dto, store);
        Store updatedStore = storeRepository.save(store);

        auditLogService.log("UPDATE_STORE", updatedStore.getId(), "Updated store: " + updatedStore.getName() + " in organization ID: " + updatedStore.getOrganization().getId());
        return storeMapper.toDto(updatedStore);
    }

    private boolean hasRoleInOrganization(User user, String roleName, UUID organizationId) {
        return userOrganizationRoleRepository.findByUserIdAndOrganizationId(user.getId(), organizationId)
                .stream()
                .anyMatch(ua -> ua.getRole().getName().equals(roleName));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getOrganizationRoles().stream()
                .anyMatch(ua -> ua.getRole().getName().equals(roleName));
    }
    }
package com.store.mgmt.organization.service;

import com.store.mgmt.config.TenantContext;
import com.store.mgmt.organization.mapper.StoreMapper;
import com.store.mgmt.organization.model.dto.CreateStoreDTO;
import com.store.mgmt.organization.model.dto.StoreDTO;
import com.store.mgmt.organization.model.dto.UpdateStoreDTO;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Organization organization = organizationRepository.findById(createDTO.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));
        if (storeRepository.findByNameAndOrganizationId(createDTO.getName(), createDTO.getOrganizationId()).isPresent()) {
            throw new IllegalArgumentException("Store name '" + createDTO.getName() + "' already exists in organization.");
        }

        // Check if user has ORG_ADMIN role for the organization
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found."));
        if (noRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(), createDTO.getOrganizationId())) {
            throw new SecurityException("User not authorized to create stores in this organization.");
        }

        Store store = storeMapper.toEntity(createDTO);
        store.setOrganization(organization);
        System.out.println(store);
        Store savedStore = storeRepository.save(store);

        System.out.println("Store saved: " + savedStore);
        logAuditEntry("CREATE_STORE", savedStore.getId(), "Created store: " + savedStore.getName() + " in organization ID: " + organization.getId());
        return storeMapper.toDto(savedStore);
    }


    @Override
    @Transactional
    public StoreDTO updateStore(UUID id, UpdateStoreDTO dto) {
        log.info("Updating store with ID: {}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found."));

        User currentUser = TenantContext.getCurrentUser();
        if (noRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(), store.getOrganization().getId())) {
            throw new SecurityException("User not authorized to update stores in this organization.");
        }

        // Update the store details
        storeMapper.updateEntityFromDto(dto, store);
        Store updatedStore = storeRepository.save(store);

        logAuditEntry("UPDATE_STORE", updatedStore.getId(), "Updated store: " + updatedStore.getName() + " in organization ID: " + updatedStore.getOrganization().getId());
        return storeMapper.toDto(updatedStore);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDTO getStoreById(UUID id) {
        log.info("Fetching store with ID: {}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found."));

        User currentUser = TenantContext.getCurrentUser();
        if (noRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(), store.getOrganization().getId()) ) {
            throw new SecurityException("User not authorized to view stores in this organization.");
        }

        return storeMapper.toDto(store);
    }

    @Override
    @Transactional
    public void deleteStore(UUID id) {
        log.info("Deleting store with ID: {}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found."));

        User currentUser = TenantContext.getCurrentUser();
        if (noRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(), store.getOrganization().getId())) {
            throw new SecurityException("User not authorized to delete stores in this organization.");
        }

        storeRepository.delete(store);
        logAuditEntry("DELETE_STORE", id, "Deleted store: " + store.getName() + " in organization ID: " + store.getOrganization().getId());
    }

    private boolean noRoleInOrganization(User user, String roleName, UUID organizationId) {
        return userOrganizationRoleRepository.findByUserIdAndOrganizationId(user.getId(), organizationId)
                .stream()
                .noneMatch(ua -> ua.getRole().getName().equals(roleName));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getOrganizationRoles().stream()
                .noneMatch(ua -> ua.getRole().getName().equals(roleName));
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
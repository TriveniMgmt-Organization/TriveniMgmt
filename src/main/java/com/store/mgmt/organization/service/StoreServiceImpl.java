package com.store.mgmt.organization.service;

import com.store.mgmt.common.service.AuthorizationService;
import com.store.mgmt.config.TenantContext;
import com.store.mgmt.organization.mapper.StoreMapper;
import com.store.mgmt.organization.model.dto.CreateStoreDTO;
import com.store.mgmt.organization.model.dto.StoreDTO;
import com.store.mgmt.organization.model.dto.UpdateStoreDTO;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.repository.OrganizationRepository;
import com.store.mgmt.organization.repository.StoreRepository;
import com.store.mgmt.users.model.RoleType;
import com.store.mgmt.users.model.entity.User;
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
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final OrganizationRepository organizationRepository;
    private final StoreMapper storeMapper;
    private final AuthorizationService authorizationService;

    public StoreServiceImpl(StoreRepository storeRepository,
                            OrganizationRepository organizationRepository,
                            UserRepository userRepository,
                            AuditLogService auditLogService,
                            StoreMapper storeMapper,
                            AuthorizationService authorizationService) {
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.storeMapper = storeMapper;
        this.authorizationService = authorizationService;
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

        // Check if user has SUPER_ADMIN role for the organization
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found."));
        authorizationService.requireRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(),
                createDTO.getOrganizationId(), "create stores in this organization");

        Store store = storeMapper.toEntity(createDTO);
        store.setOrganization(organization);
        log.debug("Creating store entity: {}", store.getName());
        Store savedStore = storeRepository.save(store);

        log.info("Store created successfully: {} (ID: {})", savedStore.getName(), savedStore.getId());
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
        authorizationService.requireRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(),
                store.getOrganization().getId(), "update stores in this organization");

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
        authorizationService.requireRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(),
                store.getOrganization().getId(), "view stores in this organization");

        return storeMapper.toDto(store);
    }

    @Override
    @Transactional
    public void deleteStore(UUID id) {
        log.info("Deleting store with ID: {}", id);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found."));

        User currentUser = TenantContext.getCurrentUser();
        authorizationService.requireRoleInOrganization(currentUser, RoleType.SUPER_ADMIN.toString(),
                store.getOrganization().getId(), "delete stores in this organization");

        storeRepository.delete(store);
        logAuditEntry("DELETE_STORE", id, "Deleted store: " + store.getName() + " in organization ID: " + store.getOrganization().getId());
    }

    private void logAuditEntry(String action, UUID entityId, String message) {
        try {
            auditLogService.builder()
                    .action(action)
                    .entityId(entityId)
                    .message(message)
                    .log();
            log.debug("Audit entry logged: action={}, entityId={}", action, entityId);
        } catch (Exception e) {
            log.error("Failed to log audit entry: action={}, entityId={}", action, entityId, e);
        }
    }
}
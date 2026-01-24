package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.exception.DuplicateStoreNameException;
import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateStoreCommand.
 */
@Component
@Transactional
public class CreateStoreHandler implements CommandHandler<CreateStoreCommand, StoreDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateStoreHandler.class);

    private final StoreRepository storeRepo;
    private final OrganizationRepository orgRepo;

    public CreateStoreHandler(StoreRepository storeRepo, OrganizationRepository orgRepo) {
        this.storeRepo = storeRepo;
        this.orgRepo = orgRepo;
    }

    @Override
    public StoreDTO handle(CreateStoreCommand cmd) {
        log.debug("Creating store: {} for organization: {}", cmd.name(), cmd.organizationId());

        // Verify organization exists
        Organization org = orgRepo.findById(cmd.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(cmd.organizationId()));

        // Check for duplicate name within organization
        if (storeRepo.existsByNameAndOrganizationId(cmd.name(), cmd.organizationId())) {
            throw new DuplicateStoreNameException(cmd.name());
        }

        Store store = new Store();
        store.setOrganization(org);
        store.setName(cmd.name());
        store.setLocation(cmd.location());
        store.setCountryCode(cmd.countryCode());
        store.setContactInfo(cmd.contactInfo());
        store.setStatus(StoreStatus.ACTIVE);

        Store saved = storeRepo.save(store);

        log.info("Created store: {} with ID: {}", saved.getName(), saved.getId());

        return toDTO(saved);
    }

    private StoreDTO toDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId())
                .organizationId(store.getOrganization().getId())
                .name(store.getName())
                .location(store.getLocation())
                .countryCode(store.getCountryCode())
                .contactInfo(store.getContactInfo())
                .status(store.getStatus().name())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}

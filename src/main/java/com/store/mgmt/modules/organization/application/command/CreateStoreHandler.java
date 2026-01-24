package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.exception.DuplicateStoreNameException;
import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.model.*;
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

        OrganizationId orgId = OrganizationId.of(cmd.organizationId());

        // Verify organization exists
        orgRepo.findById(orgId)
                .orElseThrow(() -> new OrganizationNotFoundException(orgId));

        // Check for duplicate name within organization
        if (storeRepo.existsByNameAndOrganizationId(cmd.name(), orgId)) {
            throw new DuplicateStoreNameException(cmd.name());
        }

        Store store = Store.create(
                orgId,
                cmd.name(),
                cmd.location(),
                cmd.countryCode(),
                ContactInfo.of(cmd.contactInfo())
        );

        Store saved = storeRepo.save(store);

        log.info("Created store: {} with ID: {}", saved.getName(), saved.getId().getValue());

        return toDTO(saved);
    }

    private StoreDTO toDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId().getValue())
                .organizationId(store.getOrganizationId().getValue())
                .name(store.getName())
                .location(store.getLocation())
                .countryCode(store.getCountryCode())
                .contactInfo(store.getContactInfo() != null ? store.getContactInfo().getValue() : null)
                .status(store.getStatus().name())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}

package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.exception.StoreNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateStoreCommand.
 */
@Component
@Transactional
public class UpdateStoreHandler implements CommandHandler<UpdateStoreCommand, StoreDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateStoreHandler.class);

    private final StoreRepository storeRepo;

    public UpdateStoreHandler(StoreRepository storeRepo) {
        this.storeRepo = storeRepo;
    }

    @Override
    public StoreDTO handle(UpdateStoreCommand cmd) {
        log.debug("Updating store: {}", cmd.storeId());

        Store store = storeRepo.findById(cmd.storeId())
                .orElseThrow(() -> new StoreNotFoundException(cmd.storeId()));

        store.setName(cmd.name());
        store.setLocation(cmd.location());
        store.setCountryCode(cmd.countryCode());
        store.setContactInfo(cmd.contactInfo());

        // Handle status change
        if (cmd.status() != null) {
            store.setStatus(StoreStatus.valueOf(cmd.status()));
        }

        Store saved = storeRepo.save(store);

        log.info("Updated store: {}", saved.getId());

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

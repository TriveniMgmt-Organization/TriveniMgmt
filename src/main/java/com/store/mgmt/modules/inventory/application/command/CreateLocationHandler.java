package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.domain.repository.InventoryLocationRepository;
import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.modules.inventory.application.service.LocationMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateLocationCommand.
 */
@Component
@Transactional
public class CreateLocationHandler implements CommandHandler<CreateLocationCommand, LocationResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateLocationHandler.class);

    private final InventoryLocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public CreateLocationHandler(
            InventoryLocationRepository locationRepository,
            LocationMapper locationMapper
    ) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    public LocationResponseDTO handle(CreateLocationCommand cmd) {
        log.debug("Creating location: {} for store: {}", cmd.name(), cmd.storeId());

        // Check for duplicate name within store
        locationRepository.findByNameAndStoreId(cmd.name(), cmd.storeId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Location with name '" + cmd.name() + "' already exists in this store");
        });

        InventoryLocation location = new InventoryLocation();
        location.setStoreId(cmd.storeId());
        location.setName(cmd.name());
        location.setAddress(cmd.address());
        location.setType(cmd.type());
        location.setActive(cmd.isActive() != null ? cmd.isActive() : true);

        InventoryLocation saved = locationRepository.save(location);
        log.info("Created location with ID: {}", saved.getId());

        return locationMapper.toResponseDTO(saved);
    }
}

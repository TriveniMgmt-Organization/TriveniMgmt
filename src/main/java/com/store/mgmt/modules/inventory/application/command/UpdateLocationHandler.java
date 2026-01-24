package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.domain.repository.InventoryLocationRepository;
import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.modules.inventory.application.service.LocationMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateLocationCommand.
 */
@Component
@Transactional
public class UpdateLocationHandler implements CommandHandler<UpdateLocationCommand, LocationResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateLocationHandler.class);

    private final InventoryLocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public UpdateLocationHandler(InventoryLocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    public LocationResponseDTO handle(UpdateLocationCommand cmd) {
        log.debug("Updating location: {}", cmd.id());

        InventoryLocation location = locationRepository.findByIdAndStoreId(cmd.id(), cmd.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + cmd.id()));

        // Check for duplicate name if name is being changed
        if (cmd.name() != null && !cmd.name().equals(location.getName())) {
            locationRepository.findByNameAndStoreId(cmd.name(), cmd.storeId()).ifPresent(existing -> {
                if (!existing.getId().equals(cmd.id())) {
                    throw new IllegalArgumentException("Location with name '" + cmd.name() + "' already exists in this store");
                }
            });
            location.setName(cmd.name());
        }

        if (cmd.address() != null) {
            location.setAddress(cmd.address());
        }
        if (cmd.type() != null) {
            location.setType(cmd.type());
        }
        if (cmd.isActive() != null) {
            location.setActive(cmd.isActive());
        }

        InventoryLocation saved = locationRepository.save(location);
        log.info("Updated location with ID: {}", saved.getId());

        return locationMapper.toResponseDTO(saved);
    }
}

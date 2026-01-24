package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.domain.repository.InventoryLocationRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteLocationCommand.
 */
@Component
@Transactional
public class DeleteLocationHandler implements CommandHandler<DeleteLocationCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteLocationHandler.class);

    private final InventoryLocationRepository locationRepository;

    public DeleteLocationHandler(InventoryLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Void handle(DeleteLocationCommand cmd) {
        log.debug("Deleting location: {}", cmd.id());

        InventoryLocation location = locationRepository.findByIdAndStoreId(cmd.id(), cmd.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + cmd.id()));

        // Soft delete
        location.setDeletedAt(LocalDateTime.now());
        locationRepository.save(location);

        log.info("Soft deleted location with ID: {}", cmd.id());
        return null;
    }
}

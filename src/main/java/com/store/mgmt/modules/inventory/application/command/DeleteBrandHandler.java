package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Brand;
import com.store.mgmt.modules.inventory.domain.repository.BrandRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteBrandCommand.
 */
@Component
@Transactional
public class DeleteBrandHandler implements CommandHandler<DeleteBrandCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteBrandHandler.class);

    private final BrandRepository brandRepository;

    public DeleteBrandHandler(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public Void handle(DeleteBrandCommand cmd) {
        log.debug("Deleting brand: {}", cmd.id());

        Brand brand = brandRepository.findById(cmd.id())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with ID: " + cmd.id()));

        // Soft delete
        brand.setDeletedAt(LocalDateTime.now());
        brandRepository.save(brand);

        log.info("Soft deleted brand with ID: {}", cmd.id());
        return null;
    }
}

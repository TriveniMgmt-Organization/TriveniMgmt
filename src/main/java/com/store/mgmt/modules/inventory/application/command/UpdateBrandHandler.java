package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Brand;
import com.store.mgmt.modules.inventory.domain.repository.BrandRepository;
import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import com.store.mgmt.modules.inventory.application.service.BrandMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateBrandCommand.
 */
@Component
@Transactional
public class UpdateBrandHandler implements CommandHandler<UpdateBrandCommand, BrandResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateBrandHandler.class);

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public UpdateBrandHandler(BrandRepository brandRepository, BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
    }

    @Override
    public BrandResponseDTO handle(UpdateBrandCommand cmd) {
        log.debug("Updating brand: {}", cmd.id());

        Brand brand = brandRepository.findById(cmd.id())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with ID: " + cmd.id()));

        // Check for duplicate name if name is being changed
        if (cmd.name() != null && !cmd.name().equals(brand.getName())) {
            brandRepository.findByName(cmd.name()).ifPresent(existing -> {
                if (!existing.getId().equals(cmd.id())) {
                    throw new IllegalArgumentException("Brand with name '" + cmd.name() + "' already exists");
                }
            });
            brand.setName(cmd.name());
        }

        if (cmd.description() != null) {
            brand.setDescription(cmd.description());
        }
        if (cmd.logoUrl() != null) {
            brand.setLogoUrl(cmd.logoUrl());
        }
        if (cmd.website() != null) {
            brand.setWebsite(cmd.website());
        }
        if (cmd.isActive() != null) {
            brand.setActive(cmd.isActive());
        }

        Brand saved = brandRepository.save(brand);
        log.info("Updated brand with ID: {}", saved.getId());

        return brandMapper.toResponseDTO(saved);
    }
}

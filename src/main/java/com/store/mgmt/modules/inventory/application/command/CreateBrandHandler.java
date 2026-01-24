package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Brand;
import com.store.mgmt.modules.inventory.domain.repository.BrandRepository;
import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import com.store.mgmt.modules.inventory.application.service.BrandMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateBrandCommand.
 */
@Component
@Transactional
public class CreateBrandHandler implements CommandHandler<CreateBrandCommand, BrandResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateBrandHandler.class);

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public CreateBrandHandler(BrandRepository brandRepository, BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
    }

    @Override
    public BrandResponseDTO handle(CreateBrandCommand cmd) {
        log.debug("Creating brand: {}", cmd.name());

        // Check for duplicate name
        brandRepository.findByName(cmd.name()).ifPresent(existing -> {
            throw new IllegalArgumentException("Brand with name '" + cmd.name() + "' already exists");
        });

        Brand brand = new Brand();
        brand.setName(cmd.name());
        brand.setDescription(cmd.description());
        brand.setLogoUrl(cmd.logoUrl());
        brand.setWebsite(cmd.website());
        brand.setActive(cmd.isActive() != null ? cmd.isActive() : true);

        Brand saved = brandRepository.save(brand);
        log.info("Created brand with ID: {}", saved.getId());

        return brandMapper.toResponseDTO(saved);
    }
}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Category;
import com.store.mgmt.modules.inventory.domain.repository.CategoryRepository;
import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.modules.inventory.application.service.CategoryMapper;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateCategoryCommand.
 */
@Component
@Transactional
public class CreateCategoryHandler implements CommandHandler<CreateCategoryCommand, CategoryResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateCategoryHandler.class);

    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryMapper categoryMapper;

    public CreateCategoryHandler(
            CategoryRepository categoryRepository,
            OrganizationRepository organizationRepository,
            CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.organizationRepository = organizationRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponseDTO handle(CreateCategoryCommand cmd) {
        log.debug("Creating category: {} for organization: {}", cmd.name(), cmd.organizationId());

        // Validate organization exists
        if (!organizationRepository.existsById(cmd.organizationId())) {
            throw new EntityNotFoundException("Organization not found with ID: " + cmd.organizationId());
        }

        // Check for duplicate code within organization
        categoryRepository.findByCodeAndOrganizationId(cmd.code(), cmd.organizationId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Category with code '" + cmd.code() + "' already exists in this organization");
        });

        // Check for duplicate name within organization
        categoryRepository.findByNameAndOrganizationId(cmd.name(), cmd.organizationId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Category with name '" + cmd.name() + "' already exists in this organization");
        });

        Category category = new Category();
        category.setOrganizationId(cmd.organizationId());
        category.setCode(cmd.code());
        category.setName(cmd.name());
        category.setDescription(cmd.description());
        category.setActive(cmd.isActive() != null ? cmd.isActive() : true);

        Category saved = categoryRepository.save(category);
        log.info("Created category with ID: {}", saved.getId());

        return categoryMapper.toResponseDTO(saved);
    }
}

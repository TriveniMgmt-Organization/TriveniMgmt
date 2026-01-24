package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Category;
import com.store.mgmt.modules.inventory.domain.repository.CategoryRepository;
import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.modules.inventory.application.service.CategoryMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateCategoryCommand.
 */
@Component
@Transactional
public class UpdateCategoryHandler implements CommandHandler<UpdateCategoryCommand, CategoryResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateCategoryHandler.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public UpdateCategoryHandler(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponseDTO handle(UpdateCategoryCommand cmd) {
        log.debug("Updating category: {}", cmd.id());

        Category category = categoryRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + cmd.id()));

        // Check for duplicate name if name is being changed
        if (cmd.name() != null && !cmd.name().equals(category.getName())) {
            categoryRepository.findByNameAndOrganizationId(cmd.name(), cmd.organizationId()).ifPresent(existing -> {
                if (!existing.getId().equals(cmd.id())) {
                    throw new IllegalArgumentException("Category with name '" + cmd.name() + "' already exists in this organization");
                }
            });
            category.setName(cmd.name());
        }

        if (cmd.description() != null) {
            category.setDescription(cmd.description());
        }
        if (cmd.isActive() != null) {
            category.setActive(cmd.isActive());
        }

        Category saved = categoryRepository.save(category);
        log.info("Updated category with ID: {}", saved.getId());

        return categoryMapper.toResponseDTO(saved);
    }
}

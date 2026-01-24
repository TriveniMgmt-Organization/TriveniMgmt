package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Category;
import com.store.mgmt.modules.inventory.domain.repository.CategoryRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteCategoryCommand.
 */
@Component
@Transactional
public class DeleteCategoryHandler implements CommandHandler<DeleteCategoryCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteCategoryHandler.class);

    private final CategoryRepository categoryRepository;

    public DeleteCategoryHandler(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Void handle(DeleteCategoryCommand cmd) {
        log.debug("Deleting category: {}", cmd.id());

        Category category = categoryRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + cmd.id()));

        // Soft delete
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);

        log.info("Soft deleted category with ID: {}", cmd.id());
        return null;
    }
}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Category;
import com.store.mgmt.modules.inventory.domain.repository.CategoryRepository;
import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.modules.inventory.application.service.CategoryMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetCategoryByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetCategoryByIdHandler implements QueryHandler<GetCategoryByIdQuery, CategoryResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetCategoryByIdHandler.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public GetCategoryByIdHandler(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponseDTO handle(GetCategoryByIdQuery query) {
        log.debug("Getting category by ID: {}", query.id());

        Category category = categoryRepository.findByIdAndOrganizationId(query.id(), query.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + query.id()));

        return categoryMapper.toResponseDTO(category);
    }
}

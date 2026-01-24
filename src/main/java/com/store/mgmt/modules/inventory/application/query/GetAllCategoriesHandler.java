package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Category;
import com.store.mgmt.modules.inventory.domain.repository.CategoryRepository;
import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import com.store.mgmt.modules.inventory.application.service.CategoryMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetAllCategoriesQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllCategoriesHandler implements QueryHandler<GetAllCategoriesQuery, List<CategoryResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllCategoriesHandler.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public GetAllCategoriesHandler(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryResponseDTO> handle(GetAllCategoriesQuery query) {
        log.debug("Getting all categories for organization: {}, includeInactive: {}",
                query.organizationId(), query.includeInactive());

        List<Category> categories = categoryRepository.findByOrganizationId(query.organizationId());

        if (!query.includeInactive()) {
            categories = categories.stream()
                    .filter(c -> c.isActive() && c.getDeletedAt() == null)
                    .toList();
        } else {
            categories = categories.stream()
                    .filter(c -> c.getDeletedAt() == null)
                    .toList();
        }

        return categoryMapper.toResponseDTOList(categories);
    }
}

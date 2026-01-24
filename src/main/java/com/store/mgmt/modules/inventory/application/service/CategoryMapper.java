package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.Category;
import com.store.mgmt.modules.inventory.application.dto.CategoryResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting Category entities to DTOs.
 */
@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponseDTO(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .organizationId(category.getOrganizationId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public List<CategoryResponseDTO> toResponseDTOList(List<Category> categories) {
        return categories.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

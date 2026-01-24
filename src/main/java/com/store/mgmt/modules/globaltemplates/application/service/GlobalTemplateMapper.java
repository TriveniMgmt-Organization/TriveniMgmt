package com.store.mgmt.modules.globaltemplates.application.service;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplateItem;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateItemResponseDTO;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting GlobalTemplate entities to DTOs.
 */
@Component
public class GlobalTemplateMapper {

    public GlobalTemplateResponseDTO toResponseDTO(GlobalTemplate entity) {
        if (entity == null) {
            return null;
        }

        List<GlobalTemplateItemResponseDTO> items = Collections.emptyList();
        Map<String, Integer> itemCounts = Collections.emptyMap();

        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            items = entity.getItems().stream()
                    .map(this::toItemResponseDTO)
                    .collect(Collectors.toList());

            itemCounts = entity.getItems().stream()
                    .collect(Collectors.groupingBy(
                            GlobalTemplateItem::getEntityType,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                    ));
        }

        return GlobalTemplateResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(entity.getType())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .items(items)
                .itemCounts(itemCounts)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public GlobalTemplateItemResponseDTO toItemResponseDTO(GlobalTemplateItem entity) {
        if (entity == null) {
            return null;
        }

        return GlobalTemplateItemResponseDTO.builder()
                .id(entity.getId())
                .entityType(entity.getEntityType())
                .data(entity.getData())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    public List<GlobalTemplateResponseDTO> toResponseDTOList(List<GlobalTemplate> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

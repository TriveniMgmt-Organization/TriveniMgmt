package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateItemResponseDTO;
import com.store.mgmt.modules.globaltemplates.domain.service.GlobalTemplateManagementService;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Transactional
public class AddTemplateItemHandler implements CommandHandler<AddTemplateItemCommand, GlobalTemplateResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(AddTemplateItemHandler.class);

    private final GlobalTemplateManagementService templateManagementService;

    public AddTemplateItemHandler(GlobalTemplateManagementService templateManagementService) {
        this.templateManagementService = templateManagementService;
    }

    @Override
    public GlobalTemplateResponseDTO handle(AddTemplateItemCommand cmd) {
        log.info("Adding item to template: {} - type: {}", cmd.templateId(), cmd.entityType());

        GlobalTemplate template = templateManagementService.addItemToTemplate(
                cmd.templateId(),
                cmd.entityType(),
                cmd.jsonData(),
                cmd.sortOrder() != null ? cmd.sortOrder() : 0
        );

        log.info("Added item to template: {}", cmd.templateId());

        return convertToResponseDTO(template);
    }

    private GlobalTemplateResponseDTO convertToResponseDTO(GlobalTemplate template) {
        Map<String, Integer> itemCounts = new HashMap<>();
        if (template.getItems() != null) {
            template.getItems().stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getEntityType(),
                            Collectors.counting()
                    ))
                    .forEach((k, v) -> itemCounts.put(k, v.intValue()));
        }

        return GlobalTemplateResponseDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .code(template.getCode())
                .type(template.getType())
                .description(template.getDescription())
                .isActive(template.getIsActive())
                .items(template.getItems() != null ? template.getItems().stream()
                        .map(item -> GlobalTemplateItemResponseDTO.builder()
                                .id(item.getId())
                                .entityType(item.getEntityType())
                                .data(item.getData())
                                .sortOrder(item.getSortOrder())
                                .build())
                        .collect(Collectors.toList()) : Collections.emptyList())
                .itemCounts(itemCounts)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

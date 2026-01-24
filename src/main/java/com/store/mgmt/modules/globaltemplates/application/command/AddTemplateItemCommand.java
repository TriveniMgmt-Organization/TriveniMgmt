package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

public record AddTemplateItemCommand(
        UUID templateId,
        String entityType,
        String jsonData,
        Integer sortOrder
) implements Command<GlobalTemplateResponseDTO> {
}

package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

public record UpdateTemplateCommand(
        UUID templateId,
        String name,
        String type,
        String description,
        Boolean isActive
) implements Command<GlobalTemplateResponseDTO> {
}

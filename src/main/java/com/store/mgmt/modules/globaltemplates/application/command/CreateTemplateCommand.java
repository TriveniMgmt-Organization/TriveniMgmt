package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.shared.application.command.Command;

public record CreateTemplateCommand(
        String name,
        String code,
        String type,
        String description,
        Boolean isActive
) implements Command<GlobalTemplateResponseDTO> {
}

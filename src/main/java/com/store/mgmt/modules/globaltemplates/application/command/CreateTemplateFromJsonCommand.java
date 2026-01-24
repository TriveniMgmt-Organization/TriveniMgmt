package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.shared.application.command.Command;

public record CreateTemplateFromJsonCommand(
        String jsonData
) implements Command<GlobalTemplateResponseDTO> {
}

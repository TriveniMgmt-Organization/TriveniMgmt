package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

public record DeleteTemplateCommand(
        UUID templateId
) implements Command<Void> {
}

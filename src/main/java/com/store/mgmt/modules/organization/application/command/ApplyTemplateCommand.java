package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to apply a template to an organization.
 */
public record ApplyTemplateCommand(
        UUID organizationId,
        String templateCode
) implements Command<Void> {}

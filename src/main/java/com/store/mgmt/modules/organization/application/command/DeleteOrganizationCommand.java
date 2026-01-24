package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to delete an organization.
 */
public record DeleteOrganizationCommand(
        UUID organizationId
) implements Command<Void> {}

package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to delete a role.
 */
public record DeleteRoleCommand(UUID roleId) implements Command<Void> {}

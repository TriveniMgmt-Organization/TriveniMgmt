package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to delete a user.
 */
public record DeleteUserCommand(UUID userId) implements Command<Void> {}

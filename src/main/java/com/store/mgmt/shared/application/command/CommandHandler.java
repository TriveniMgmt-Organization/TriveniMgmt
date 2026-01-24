package com.store.mgmt.shared.application.command;

/**
 * Handler for a specific command type.
 * Each command should have exactly one handler.
 *
 * @param <C> The command type
 * @param <R> The result type
 */
public interface CommandHandler<C extends Command<R>, R> {

    /**
     * Handle the command and return a result.
     *
     * @param command The command to handle
     * @return The result of handling the command
     */
    R handle(C command);
}

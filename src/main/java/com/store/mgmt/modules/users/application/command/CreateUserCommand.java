package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.shared.application.command.Command;

/**
 * Command to create a new user.
 */
public record CreateUserCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) implements Command<UserDTO> {}

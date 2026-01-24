package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update a user.
 */
public record UpdateUserCommand(
        UUID userId,
        String firstName,
        String lastName,
        String imageUrl,
        Boolean active
) implements Command<UserDTO> {}

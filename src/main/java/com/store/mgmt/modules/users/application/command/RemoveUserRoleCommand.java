package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to remove a role from a user.
 */
public record RemoveUserRoleCommand(
        UUID userId,
        UUID roleId,
        UUID organizationId,
        UUID storeId  // nullable
) implements Command<UserDTO> {}

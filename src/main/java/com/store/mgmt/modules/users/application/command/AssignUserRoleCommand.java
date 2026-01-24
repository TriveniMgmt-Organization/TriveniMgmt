package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to assign a role to a user within an organization.
 */
public record AssignUserRoleCommand(
        UUID userId,
        UUID roleId,
        UUID organizationId,
        UUID storeId  // nullable - if null, role applies to entire organization
) implements Command<UserDTO> {}

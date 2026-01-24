package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update a role.
 */
public record UpdateRoleCommand(
        UUID roleId,
        String name,
        String description
) implements Command<RoleDTO> {}

package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.shared.application.command.Command;

/**
 * Command to create a new role.
 */
public record CreateRoleCommand(
        String name,
        String description
) implements Command<RoleDTO> {}

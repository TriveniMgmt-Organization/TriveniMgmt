package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to remove a permission from a role.
 */
public record RemovePermissionFromRoleCommand(
        UUID roleId,
        UUID permissionId
) implements Command<RoleDTO> {}

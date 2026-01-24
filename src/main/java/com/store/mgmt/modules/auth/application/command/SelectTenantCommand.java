package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.shared.application.command.Command;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

/**
 * Command to select the active organization and store for the user session.
 */
public record SelectTenantCommand(
        UUID organizationId,
        UUID storeId,
        HttpServletResponse response
) implements Command<AuthResponseDTO> {
}

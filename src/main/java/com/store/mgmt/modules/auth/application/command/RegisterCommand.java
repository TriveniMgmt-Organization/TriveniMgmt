package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.shared.application.command.Command;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Command to register a new user.
 */
public record RegisterCommand(
        String firstName,
        String lastName,
        String email,
        String password,
        String invitationToken,
        String templateCode,
        HttpServletResponse response
) implements Command<AuthResponseDTO> {
}

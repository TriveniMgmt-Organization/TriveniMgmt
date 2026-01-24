package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.shared.application.command.Command;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Command to authenticate a user.
 */
public record LoginCommand(
        String username,
        String password,
        boolean rememberMe,
        HttpServletResponse response
) implements Command<AuthResponseDTO> {
}

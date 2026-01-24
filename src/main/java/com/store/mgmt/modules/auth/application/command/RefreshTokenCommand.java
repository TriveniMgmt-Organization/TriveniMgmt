package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.shared.application.command.Command;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Command to refresh the access token using a refresh token.
 */
public record RefreshTokenCommand(
        String refreshToken,
        HttpServletResponse response
) implements Command<AuthResponseDTO> {
}

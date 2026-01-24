package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.shared.application.command.Command;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Command to logout a user.
 */
public record LogoutCommand(
        String refreshToken,
        HttpServletResponse response
) implements Command<Void> {
}

package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.modules.auth.domain.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for LogoutCommand.
 * Invalidates refresh token and clears auth cookies.
 */
@Component
@Transactional
public class LogoutHandler implements CommandHandler<LogoutCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(LogoutHandler.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthCookieService authCookieService;

    public LogoutHandler(
            RefreshTokenRepository refreshTokenRepository,
            AuthCookieService authCookieService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.authCookieService = authCookieService;
    }

    @Override
    public Void handle(LogoutCommand cmd) {
        log.info("Processing logout request");

        try {
            if (cmd.refreshToken() != null) {
                refreshTokenRepository.deleteByToken(cmd.refreshToken());
                log.debug("Refresh token invalidated");
            }
        } catch (Exception e) {
            log.error("Error invalidating refresh token during logout", e);
            // Continue with cookie cleanup even if token deletion fails
        }

        authCookieService.clearAuthCookies(cmd.response());
        log.info("User logged out successfully");

        return null;
    }
}

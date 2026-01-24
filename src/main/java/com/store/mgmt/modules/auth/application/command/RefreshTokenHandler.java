package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.domain.model.RefreshToken;
import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.application.service.AuthContextService.TokenPair;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.modules.auth.domain.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Handler for RefreshTokenCommand.
 * Refreshes the access token using a valid refresh token.
 */
@Component
@Transactional
public class RefreshTokenHandler implements CommandHandler<RefreshTokenCommand, AuthResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenHandler.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;
    private final AuthCookieService authCookieService;

    public RefreshTokenHandler(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            AuthContextService authContextService,
            AuthCookieService authCookieService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
        this.authCookieService = authCookieService;
    }

    @Override
    public AuthResponseDTO handle(RefreshTokenCommand cmd) {
        log.debug("Processing token refresh request");

        if (cmd.refreshToken() == null) {
            throw new JwtException("No refresh token provided");
        }

        // Find and validate refresh token (eager fetch user with roles)
        RefreshToken storedToken = refreshTokenRepository.findByTokenWithUser(cmd.refreshToken())
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token - not found");
                    return new JwtException("Invalid refresh token");
                });

        if (storedToken.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(storedToken);
            throw new JwtException("Refresh token expired");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> {
                    log.warn("User not found for refresh token");
                    return new JwtException("Invalid refresh token - user not found");
                });

        // Invalidate old refresh token (rotation)
        refreshTokenRepository.delete(storedToken);

        // Generate new tokens
        ActiveContext context = authContextService.determineActiveContext(user);
        TokenPair tokens = authContextService.generateTokens(user, context);

        // Set cookies
        authCookieService.setAuthCookies(tokens.accessToken(), tokens.refreshToken(), cmd.response());

        log.debug("Token refreshed for user: {}", user.getEmail());

        return AuthResponseDTO.builder()
                .user(authContextService.buildAuthUserDTO(user, context))
                .build();
    }
}

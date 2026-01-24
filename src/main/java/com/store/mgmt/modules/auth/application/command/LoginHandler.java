package com.store.mgmt.modules.auth.application.command;

import com.store.mgmt.modules.auth.application.dto.AuthResponseDTO;
import com.store.mgmt.modules.auth.application.service.AuthContextService;
import com.store.mgmt.modules.auth.application.service.AuthContextService.ActiveContext;
import com.store.mgmt.modules.auth.application.service.AuthContextService.TokenPair;
import com.store.mgmt.modules.auth.infrastructure.service.AuthCookieService;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for LoginCommand.
 * Authenticates user and issues JWT tokens.
 */
@Component
@Transactional
public class LoginHandler implements CommandHandler<LoginCommand, AuthResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;
    private final AuthCookieService authCookieService;

    public LoginHandler(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            AuthContextService authContextService,
            AuthCookieService authCookieService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
        this.authCookieService = authCookieService;
    }

    @Override
    public AuthResponseDTO handle(LoginCommand cmd) {
        log.info("Authenticating user: {}", cmd.username());

        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(cmd.username(), cmd.password())
            );

            // Fetch user with all related data to avoid N+1 queries
            User user = userRepository.findByUsernameWithAllRelatedData(cmd.username())
                    .or(() -> userRepository.findByEmailWithRolesAndPermissions(cmd.username()))
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            if (!user.isActive()) {
                log.warn("User account inactive: {}", cmd.username());
                throw new DisabledException("User account is inactive");
            }

            // Determine active context and generate tokens
            ActiveContext context = authContextService.determineActiveContext(user);
            TokenPair tokens = authContextService.generateTokens(user, context);

            // Set cookies
            authCookieService.setAuthCookies(tokens.accessToken(), tokens.refreshToken(), cmd.response());

            log.info("User {} successfully authenticated", cmd.username());

            return AuthResponseDTO.builder()
                    .user(authContextService.buildAuthUserDTO(user, context))
                    .build();

        } catch (BadCredentialsException | DisabledException e) {
            log.warn("Authentication failed for user {}: {}", cmd.username(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", cmd.username(), e);
            throw new BadCredentialsException("Authentication failed", e);
        }
    }
}

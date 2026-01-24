package com.store.mgmt.modules.auth.application.query;

import com.store.mgmt.auth.service.JWTService;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for ValidateTokenQuery.
 * Validates if the provided access token is valid.
 */
@Component
@Transactional(readOnly = true)
public class ValidateTokenHandler implements QueryHandler<ValidateTokenQuery, Boolean> {

    private static final Logger log = LoggerFactory.getLogger(ValidateTokenHandler.class);

    private final JWTService jwtService;
    private final UserRepository userRepository;

    public ValidateTokenHandler(JWTService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Boolean handle(ValidateTokenQuery query) {
        if (query.accessToken() == null || query.accessToken().isEmpty()) {
            log.warn("No access token provided for validation");
            return false;
        }

        try {
            JWTService.JwtData jwtData = jwtService.extractJwtData(query.accessToken());
            String email = jwtData.username;
            log.info("Validating token for user: {}", email);

            User user = userRepository.findByEmailWithRolesAndPermissions(email)
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            boolean isValid = jwtService.validateToken(query.accessToken(), user);

            if (!isValid) {
                log.warn("Invalid or expired token for user: {}", email);
            }

            return isValid;
        } catch (JwtException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error during token validation: {}", e.getMessage());
            return false;
        }
    }
}

package com.store.mgmt.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates critical application configuration on startup.
 * Logs warnings for missing or potentially misconfigured values.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationStartupValidator {

    private final Environment environment;
    private final DataSource dataSource;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${FRONTEND_URL:}")
    private String frontendUrl;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @EventListener(ApplicationReadyEvent.class)
    public void validateOnStartup() {
        log.info("=== Application Startup Validation ===");
        log.info("Active profile: {}", activeProfile);

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Validate JWT configuration
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            errors.add("JWT_SECRET is not configured");
        } else if (jwtSecret.length() < 32) {
            warnings.add("JWT_SECRET is shorter than recommended 32 characters");
        }
        if (jwtSecret != null && jwtSecret.contains("dev-only") && !isDevProfile()) {
            errors.add("Development JWT secret is being used in non-dev profile!");
        }

        // Validate CORS configuration
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            errors.add("FRONTEND_URL is not configured");
        } else if (frontendUrl.contains("localhost") && isProdProfile()) {
            warnings.add("FRONTEND_URL contains 'localhost' in production profile");
        }

        // Validate database connection
        try (Connection conn = dataSource.getConnection()) {
            log.info("Database connection: OK ({})", conn.getMetaData().getURL());
        } catch (SQLException e) {
            errors.add("Database connection failed: " + e.getMessage());
        }

        // Log warnings
        for (String warning : warnings) {
            log.warn("Configuration warning: {}", warning);
        }

        // Log errors
        for (String error : errors) {
            log.error("Configuration error: {}", error);
        }

        if (errors.isEmpty()) {
            log.info("=== Startup validation completed successfully ===");
        } else {
            log.error("=== Startup validation completed with {} error(s) ===", errors.size());
            if (isProdProfile()) {
                throw new IllegalStateException("Critical configuration errors detected in production. Fix the following: " + String.join(", ", errors));
            }
        }
    }

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private boolean isProdProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}

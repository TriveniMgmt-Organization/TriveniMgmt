package com.store.mgmt.config.security;

import org.springframework.context.annotation.Configuration;

/**
 * Rate limiting configuration.
 * TODO: Enable Bucket4j dependency in build.gradle.kts and implement rate limiting
 * for sensitive endpoints (login, password reset, etc.).
 */
@Configuration
public class RateLimitConfig {
    // Rate limiting to be implemented using Bucket4j
}

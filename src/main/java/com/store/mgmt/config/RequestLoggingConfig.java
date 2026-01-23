package com.store.mgmt.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuration for request logging.
 * Enabled by setting logging.request.enabled=true in application properties.
 * Useful for debugging API calls in development.
 */
@Configuration
@ConditionalOnProperty(name = "logging.request.enabled", havingValue = "true")
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false); // Don't log headers (may contain auth tokens)
        filter.setIncludeClientInfo(true);
        filter.setBeforeMessagePrefix("REQUEST: ");
        filter.setAfterMessagePrefix("RESPONSE: ");
        return filter;
    }
}

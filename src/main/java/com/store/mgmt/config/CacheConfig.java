package com.store.mgmt.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for high-performance caching.
 *
 * Caches:
 * - permissions: All permissions (rarely changes, 15 min TTL)
 * - roles: All roles with their permissions (rarely changes, 15 min TTL)
 * - userPermissions: User+org specific permissions (15 min TTL, evicted on role changes)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PERMISSIONS_CACHE = "permissions";
    public static final String ROLES_CACHE = "roles";
    public static final String USER_PERMISSIONS_CACHE = "userPermissions";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setCacheNames(java.util.List.of(
                PERMISSIONS_CACHE,
                ROLES_CACHE,
                USER_PERMISSIONS_CACHE
        ));
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats();
    }
}

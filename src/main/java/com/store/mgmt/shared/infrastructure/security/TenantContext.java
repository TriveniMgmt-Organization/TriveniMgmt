package com.store.mgmt.shared.infrastructure.security;

import com.store.mgmt.shared.domain.exception.AuthorizationException;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Holds the current tenant context for the request.
 * This is set by the TenantContextFilter and used throughout the request lifecycle.
 */
public record TenantContext(
        UUID organizationId,
        UUID storeId,
        UUID userId,
        String username
) {

    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    public TenantContext {
        // organizationId and storeId can be null for some operations
        Objects.requireNonNull(userId, "User ID cannot be null");
    }

    /**
     * Get the current tenant context or throw if not set.
     */
    public static TenantContext current() {
        TenantContext ctx = CURRENT.get();
        if (ctx == null) {
            throw new TenantContextNotSetException();
        }
        return ctx;
    }

    /**
     * Get the current tenant context if set.
     */
    public static Optional<TenantContext> currentOptional() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * Check if a tenant context is set.
     */
    public static boolean isSet() {
        return CURRENT.get() != null;
    }

    /**
     * Set the current tenant context.
     */
    public static void set(TenantContext context) {
        CURRENT.set(context);
    }

    /**
     * Clear the current tenant context.
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Require that the current context matches the expected organization.
     */
    public void requireOrganization(UUID expectedOrgId) {
        if (!Objects.equals(this.organizationId, expectedOrgId)) {
            throw new AuthorizationException(
                    String.format("Access denied: organization %s does not match context %s",
                            expectedOrgId, this.organizationId)
            );
        }
    }

    /**
     * Require that the current context matches the expected store.
     */
    public void requireStore(UUID expectedStoreId) {
        if (!Objects.equals(this.storeId, expectedStoreId)) {
            throw new AuthorizationException(
                    String.format("Access denied: store %s does not match context %s",
                            expectedStoreId, this.storeId)
            );
        }
    }

    /**
     * Check if the context has access to the given store.
     */
    public boolean hasStoreAccess(UUID checkStoreId) {
        return Objects.equals(this.storeId, checkStoreId);
    }

    /**
     * Check if the context has access to the given organization.
     */
    public boolean hasOrganizationAccess(UUID checkOrgId) {
        return Objects.equals(this.organizationId, checkOrgId);
    }

    /**
     * Exception thrown when tenant context is not set.
     */
    public static class TenantContextNotSetException extends RuntimeException {
        public TenantContextNotSetException() {
            super("Tenant context is not set for this request");
        }
    }
}

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
     * Get the store ID, throwing an exception if not in store context.
     * Use this for operations that require a store context.
     */
    public UUID requireStoreId() {
        if (this.storeId == null) {
            throw new StoreContextRequiredException();
        }
        return this.storeId;
    }

    /**
     * Get the organization ID, throwing an exception if not set.
     * Use this for operations that require an organization context.
     */
    public UUID requireOrganizationId() {
        if (this.organizationId == null) {
            throw new OrganizationContextRequiredException();
        }
        return this.organizationId;
    }

    /**
     * Exception thrown when tenant context is not set.
     */
    public static class TenantContextNotSetException extends RuntimeException {
        public TenantContextNotSetException() {
            super("Tenant context is not set for this request");
        }
    }

    /**
     * Exception thrown when store context is required but not set.
     */
    public static class StoreContextRequiredException extends RuntimeException {
        public StoreContextRequiredException() {
            super("This operation requires a store context. Please select a store first.");
        }
    }

    /**
     * Exception thrown when organization context is required but not set.
     */
    public static class OrganizationContextRequiredException extends RuntimeException {
        public OrganizationContextRequiredException() {
            super("This operation requires an organization context.");
        }
    }
}

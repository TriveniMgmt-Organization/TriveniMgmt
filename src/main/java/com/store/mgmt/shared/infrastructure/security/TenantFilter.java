package com.store.mgmt.shared.infrastructure.security;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service to enable/disable Hibernate tenant filters.
 * Uses @FilterDef and @Filter annotations on entities to automatically
 * filter queries by organization_id.
 */
@Component
public class TenantFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    public static final String FILTER_NAME = "tenantFilter";
    public static final String PARAM_ORGANIZATION_ID = "organizationId";

    private final EntityManager entityManager;

    public TenantFilter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Enable the tenant filter for the current session.
     * @param organizationId The organization ID to filter by
     */
    public void enableFilter(UUID organizationId) {
        if (organizationId == null) {
            log.debug("Organization ID is null, not enabling tenant filter");
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter(FILTER_NAME)
                .setParameter(PARAM_ORGANIZATION_ID, organizationId);
        log.debug("Tenant filter enabled for organization: {}", organizationId);
    }

    /**
     * Disable the tenant filter for the current session.
     */
    public void disableFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter(FILTER_NAME);
        log.debug("Tenant filter disabled");
    }

    /**
     * Check if the tenant filter is enabled.
     */
    public boolean isFilterEnabled() {
        Session session = entityManager.unwrap(Session.class);
        return session.getEnabledFilter(FILTER_NAME) != null;
    }

    /**
     * Enable filter using the current TenantContext.
     */
    public void enableFromContext() {
        TenantContext.currentOptional().ifPresent(ctx -> {
            if (ctx.organizationId() != null) {
                enableFilter(ctx.organizationId());
            }
        });
    }
}

package com.store.mgmt.config;

import com.store.mgmt.shared.infrastructure.security.TenantContext;
import com.store.mgmt.shared.infrastructure.security.TenantFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aspect that automatically enables the Hibernate tenant filter
 * for all repository operations when a TenantContext is set.
 *
 * This ensures automatic tenant isolation at the database level.
 */
@Aspect
@Component
@Order(1)
public class TenantFilterAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantFilterAspect.class);

    private final TenantFilter tenantFilter;

    public TenantFilterAspect(TenantFilter tenantFilter) {
        this.tenantFilter = tenantFilter;
    }

    /**
     * Enable tenant filter for all repository method calls in the inventory module.
     */
    @Around("execution(* com.store.mgmt.modules.inventory.domain.repository.*.*(..))")
    public Object enableTenantFilterForInventory(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithTenantFilter(joinPoint);
    }

    /**
     * Enable tenant filter for all JPA repository method calls.
     */
    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public Object enableTenantFilterForJpa(ProceedingJoinPoint joinPoint) throws Throwable {
        return executeWithTenantFilter(joinPoint);
    }

    private Object executeWithTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        TenantContext ctx = TenantContext.currentOptional().orElse(null);

        if (ctx != null && ctx.organizationId() != null && !tenantFilter.isFilterEnabled()) {
            tenantFilter.enableFilter(ctx.organizationId());
            log.debug("Enabled tenant filter for repository call: {} (org: {})",
                    joinPoint.getSignature().toShortString(), ctx.organizationId());
        }

        return joinPoint.proceed();
    }
}

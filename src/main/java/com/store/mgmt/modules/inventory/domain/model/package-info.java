/**
 * Inventory domain model package.
 *
 * Defines the Hibernate tenant filter for multi-tenant isolation.
 */
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "organizationId", type = UUID.class)
)
package com.store.mgmt.modules.inventory.domain.model;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

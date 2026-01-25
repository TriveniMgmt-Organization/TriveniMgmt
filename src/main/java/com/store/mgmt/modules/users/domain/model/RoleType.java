package com.store.mgmt.modules.users.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration of role types in the system.
 *
 * Platform Scope:
 * - SUPER_ADMIN: Platform administrator with full system access
 *
 * Organization Scope:
 * - ORG_ADMIN: Organization owner with full access to organization settings and users
 * - ACCOUNTANT: Financial reporting and accounting access at organization level
 *
 * Store Scope:
 * - STORE_MANAGER: Full store management access including staff, inventory, and operations
 * - SHIFT_LEAD: Shift supervisor with limited management capabilities
 * - CASHIER: Point-of-sale operations and basic inventory viewing
 * - INVENTORY_SPECIALIST: Stock management, receiving, and inventory operations
 * - PURCHASING_AGENT: Vendor management and purchase order operations
 * - STAFF: General read access for basic store operations
 */
@Schema(name = "RoleType", description = "Enumeration of different user roles in the system")
public enum RoleType {
    // Platform scope
    SUPER_ADMIN,

    // Organization scope
    ORG_ADMIN,
    ACCOUNTANT,

    // Store scope
    STORE_MANAGER,
    SHIFT_LEAD,
    CASHIER,
    INVENTORY_SPECIALIST,
    PURCHASING_AGENT,
    STAFF
}

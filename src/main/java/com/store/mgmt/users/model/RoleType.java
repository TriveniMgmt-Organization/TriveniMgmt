package com.store.mgmt.users.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RoleType", description = "Enumeration of different user roles in the system")
public enum RoleType {
    ORG_ADMIN, STORE_MANAGER, CASHIER, SUPER_ADMIN, STAFF, CUSTOMER
}


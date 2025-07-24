package com.store.mgmt.users.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name= "PermissionType", description = "Enumeration of different permission types for user roles in the system")
public enum PermissionType {
    PRODUCT_READ,
    PRODUCT_WRITE,
    USER_READ,
    USER_WRITE,
    ROLE_READ,
    ROLE_WRITE,
    INVENTORY_ITEM_READ,
    INVENTORY_ITEM_WRITE,
    CATEGORY_READ,
    CATEGORY_WRITE,
    SUPPLIER_READ,
    SUPPLIER_WRITE,
    LOCATION_READ,
    LOCATION_WRITE,
    UOM_READ,
    UOM_WRITE,
    PO_READ,
    PO_WRITE,
    SALE_READ,
    SALE_WRITE,
    DISCOUNT_READ,
    DISCOUNT_WRITE,
    DAMAGE_LOSS_READ,
    DAMAGE_LOSS_WRITE,
    STOCK_CHECK_READ,
    REPORT_READ,
    ORG_READ,
    ORG_WRITE,
    STORE_WRITE,
    STORE_READ
}

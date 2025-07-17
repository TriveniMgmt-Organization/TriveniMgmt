package com.store.mgmt.utils;

public class PermissionType {
    private PermissionType() {
        // Private constructor to prevent instantiation
    }

    public static final String USER_READ = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";
    public static final String CATEGORY_READ = "CATEGORY_READ";
    public static final String CATEGORY_WRITE = "CATEGORY_WRITE";

    public static final String DAMAGE_LOSS_READ = "DAMAGE_LOSS_READ";
    public static final String DAMAGE_LOSS_WRITE = "DAMAGE_LOSS_WRITE";

    public static final String DISCOUNT_READ = "DISCOUNT_READ";
    public static final String DISCOUNT_WRITE = "DISCOUNT_WRITE";

    public static final String INVENTORY_ITEM_READ = "INVENTORY_ITEM_READ";
    public static final String INVENTORY_ITEM_WRITE = "INVENTORY_ITEM_WRITE";

    public static final String LOCATION_READ = "LOCATION_READ";
    public static final String LOCATION_WRITE = "LOCATION_WRITE";

    public static final String PO_READ = "PO_READ";
    public static final String PO_WRITE = "PO_WRITE";

    public static final String PRODUCT_READ = "PRODUCT_READ";
    public static final String PRODUCT_WRITE = "PRODUCT_WRITE";

    public static final String REPORT_READ = "REPORT_READ";

    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_WRITE = "ROLE_WRITE";

    public static final String SALE_READ = "SALE_READ";
    public static final String SALE_WRITE = "SALE_WRITE";

    public static final String STOCK_CHECK_READ = "STOCK_CHECK_READ";

    public static final String SUPPLIER_READ = "SUPPLIER_READ";
    public static final String SUPPLIER_WRITE = "SUPPLIER_WRITE";

    public static final String UOM_READ = "UOM_READ";
    public static final String UOM_WRITE = "UOM_WRITE";
}

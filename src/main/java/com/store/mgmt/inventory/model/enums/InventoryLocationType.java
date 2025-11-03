package com.store.mgmt.inventory.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InventoryLocationType {
    STORE("store"),
    WAREHOUSE("warehouse"),
    SHELF_AREA("shelf_area"),
    COLD_STORAGE("cold_storage"),
    OTHER("other");
    private final String value;
    InventoryLocationType(String value) {
        this.value = value;
    }
    @JsonValue
    public String getValue() {
        return value;
    }
}

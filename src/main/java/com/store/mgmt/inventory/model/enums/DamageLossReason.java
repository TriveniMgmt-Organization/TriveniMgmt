package com.store.mgmt.inventory.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DamageLossReason {
    EXPIRED("expired"),
    DAMAGED_IN_TRANSIT("damaged_in_transit"),
    SPOILAGE("spoilage"),
    THEFT("theft"),
    RETURNED_DAMAGED("returned_damaged"),
    OTHER("other");

    private final String value;

    DamageLossReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

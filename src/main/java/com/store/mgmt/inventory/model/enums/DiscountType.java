package com.store.mgmt.inventory.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DiscountType {
    PERCENTAGE(
            "percentage" // e.g., 10% off
    ),
    FIXED_AMOUNT(
            "fixed_amount" // e.g., $5 off
    ),
    BOGO("bogo"), // Buy One Get One (specific logic handled in service)
    BUNDLE("bundle"); // Bundle discount (specific logic handled in service)

    private final String value;

    DiscountType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

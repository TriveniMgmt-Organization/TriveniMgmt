package com.store.mgmt.inventory.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the status of a purchase order.
 * This enum is used to track the lifecycle of a purchase order in the inventory management system.
 */
@Schema(
        name = "PurchaseOrderStatus",
        description = "Enum representing the status of a purchase order in the inventory management system.")
public enum PurchaseOrderStatus {

    PENDING(
            "pending"
    ),
    ORDERED(
            "ordered"
    ),
    RECEIVED_PARTIAL(
            "received_partial"
    ),
    RECEIVED_COMPLETE(
            "received_complete"),
    CANCELLED(
            "cancelled"
    );
    private final String value;
    PurchaseOrderStatus(String value) {
        this.value = value;
    }
    @JsonValue
    public String getValue() {
        return value;
    }

}

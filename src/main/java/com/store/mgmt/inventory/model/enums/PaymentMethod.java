package com.store.mgmt.inventory.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CASH("cash"),
    CREDIT_CARD("credit_card"),
    DEBIT_CARD("debit_card"),
    MOBILE_PAY("mobile_pay"),
    GIFT_CARD("gift_card"),
    OTHER("other");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

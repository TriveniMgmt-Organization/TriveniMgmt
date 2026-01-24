package com.store.mgmt.modules.organization.domain.model;

public enum StoreStatus {
    ACTIVE, INACTIVE, CLOSED;

    public boolean isOperational() {
        return this == ACTIVE;
    }

    public boolean canReactivate() {
        return this == INACTIVE;
    }
}

package com.store.mgmt.modules.organization.domain.event;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a new store is created.
 */
public final class StoreCreated extends BaseDomainEvent {

    private final StoreId storeId;
    private final OrganizationId organizationId;
    private final String name;

    public StoreCreated(StoreId storeId, OrganizationId organizationId, String name) {
        super(storeId.getValue(), "Store");
        this.storeId = storeId;
        this.organizationId = organizationId;
        this.name = name;
    }

    public StoreId getStoreId() {
        return storeId;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }
}

package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.modules.organization.domain.event.StoreCreated;
import com.store.mgmt.modules.organization.domain.event.StoreStatusChanged;
import com.store.mgmt.shared.domain.model.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Store aggregate root - represents a physical or virtual store within an organization.
 */
public class Store extends AggregateRoot<StoreId> {

    private final StoreId id;
    private OrganizationId organizationId;
    private String name;
    private String location;
    private String countryCode;
    private ContactInfo contactInfo;
    private StoreStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Store(StoreId id) {
        this.id = id;
        this.status = StoreStatus.ACTIVE;
    }

    @Override
    public StoreId getId() {
        return id;
    }

    /**
     * Factory method to create a new store.
     */
    public static Store create(
            OrganizationId organizationId,
            String name,
            String location,
            String countryCode,
            ContactInfo contactInfo
    ) {
        Objects.requireNonNull(organizationId, "Organization ID is required");
        Objects.requireNonNull(name, "Name is required");

        Store store = new Store(StoreId.generate());
        store.organizationId = organizationId;
        store.name = name.trim();
        store.location = location;
        store.countryCode = countryCode;
        store.contactInfo = contactInfo;
        store.createdAt = LocalDateTime.now();
        store.updatedAt = store.createdAt;

        store.registerEvent(new StoreCreated(store.id, store.organizationId, store.name));

        return store;
    }

    /**
     * Reconstitute from persistence.
     */
    public static Store reconstitute(
            StoreId id,
            OrganizationId organizationId,
            String name,
            String location,
            String countryCode,
            ContactInfo contactInfo,
            StoreStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        Store store = new Store(id);
        store.organizationId = organizationId;
        store.name = name;
        store.location = location;
        store.countryCode = countryCode;
        store.contactInfo = contactInfo;
        store.status = status != null ? status : StoreStatus.ACTIVE;
        store.createdAt = createdAt;
        store.updatedAt = updatedAt;
        store.deletedAt = deletedAt;
        return store;
    }

    // ==================== Commands ====================

    public void updateDetails(String name, String location, String countryCode, ContactInfo contactInfo) {
        if (name != null && !name.equals(this.name)) {
            this.name = name.trim();
        }
        if (location != null) {
            this.location = location;
        }
        if (countryCode != null) {
            this.countryCode = countryCode;
        }
        if (contactInfo != null) {
            this.contactInfo = contactInfo;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.status != StoreStatus.ACTIVE) {
            StoreStatus oldStatus = this.status;
            this.status = StoreStatus.ACTIVE;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new StoreStatusChanged(id, oldStatus, StoreStatus.ACTIVE));
        }
    }

    public void deactivate() {
        if (this.status != StoreStatus.INACTIVE) {
            StoreStatus oldStatus = this.status;
            this.status = StoreStatus.INACTIVE;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new StoreStatusChanged(id, oldStatus, StoreStatus.INACTIVE));
        }
    }

    public void close() {
        if (this.status != StoreStatus.CLOSED) {
            StoreStatus oldStatus = this.status;
            this.status = StoreStatus.CLOSED;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new StoreStatusChanged(id, oldStatus, StoreStatus.CLOSED));
        }
    }

    public void delete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.updatedAt = this.deletedAt;
        }
    }

    // ==================== Queries ====================

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isOperational() {
        return status.isOperational();
    }

    public boolean canReactivate() {
        return status.canReactivate();
    }

    // ==================== Getters ====================

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public StoreStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}

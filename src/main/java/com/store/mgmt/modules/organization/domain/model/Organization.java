package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.modules.organization.domain.event.OrganizationCreated;
import com.store.mgmt.modules.organization.domain.event.OrganizationDeleted;
import com.store.mgmt.modules.organization.domain.event.OrganizationUpdated;
import com.store.mgmt.modules.organization.domain.event.TemplateApplied;
import com.store.mgmt.modules.organization.domain.exception.TemplateAlreadyAppliedException;
import com.store.mgmt.shared.domain.model.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Organization aggregate root - represents a tenant in the multi-tenant system.
 */
public class Organization extends AggregateRoot<OrganizationId> {

    private final OrganizationId id;
    private String name;
    private String description;
    private ContactInfo contactInfo;
    private String appliedTemplateCode;
    private final List<StoreId> storeIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Organization(OrganizationId id) {
        this.id = id;
        this.storeIds = new ArrayList<>();
    }

    @Override
    public OrganizationId getId() {
        return id;
    }

    /**
     * Factory method to create a new organization.
     */
    public static Organization create(String name, String description, ContactInfo contactInfo, UserId createdBy) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(createdBy, "Created by user is required");

        Organization org = new Organization(OrganizationId.generate());
        org.name = name.trim();
        org.description = description;
        org.contactInfo = contactInfo;
        org.createdAt = LocalDateTime.now();
        org.updatedAt = org.createdAt;

        org.registerEvent(new OrganizationCreated(org.id, org.name, createdBy));

        return org;
    }

    /**
     * Reconstitute from persistence.
     */
    public static Organization reconstitute(
            OrganizationId id,
            String name,
            String description,
            ContactInfo contactInfo,
            String appliedTemplateCode,
            List<StoreId> storeIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        Organization org = new Organization(id);
        org.name = name;
        org.description = description;
        org.contactInfo = contactInfo;
        org.appliedTemplateCode = appliedTemplateCode;
        if (storeIds != null) {
            org.storeIds.addAll(storeIds);
        }
        org.createdAt = createdAt;
        org.updatedAt = updatedAt;
        org.deletedAt = deletedAt;
        return org;
    }

    // ==================== Commands ====================

    public void updateDetails(String name, String description, ContactInfo contactInfo) {
        StringBuilder updatedFields = new StringBuilder();

        if (name != null && !name.equals(this.name)) {
            this.name = name.trim();
            updatedFields.append("name,");
        }
        if (description != null && !description.equals(this.description)) {
            this.description = description;
            updatedFields.append("description,");
        }
        if (contactInfo != null) {
            this.contactInfo = contactInfo;
            updatedFields.append("contactInfo,");
        }

        this.updatedAt = LocalDateTime.now();

        if (updatedFields.length() > 0) {
            registerEvent(new OrganizationUpdated(id, updatedFields.toString()));
        }
    }

    public void applyTemplate(String templateCode) {
        Objects.requireNonNull(templateCode, "Template code is required");

        if (this.appliedTemplateCode != null) {
            throw new TemplateAlreadyAppliedException(this.appliedTemplateCode);
        }

        this.appliedTemplateCode = templateCode;
        this.updatedAt = LocalDateTime.now();

        registerEvent(new TemplateApplied(id, templateCode));
    }

    public void delete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.updatedAt = this.deletedAt;
            registerEvent(new OrganizationDeleted(id));
        }
    }

    public void addStore(StoreId storeId) {
        if (!storeIds.contains(storeId)) {
            storeIds.add(storeId);
        }
    }

    public void removeStore(StoreId storeId) {
        storeIds.remove(storeId);
    }

    // ==================== Queries ====================

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean hasTemplate() {
        return appliedTemplateCode != null;
    }

    public boolean hasStores() {
        return !storeIds.isEmpty();
    }

    public int getStoreCount() {
        return storeIds.size();
    }

    // ==================== Getters ====================

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public String getAppliedTemplateCode() {
        return appliedTemplateCode;
    }

    public List<StoreId> getStoreIds() {
        return Collections.unmodifiableList(storeIds);
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

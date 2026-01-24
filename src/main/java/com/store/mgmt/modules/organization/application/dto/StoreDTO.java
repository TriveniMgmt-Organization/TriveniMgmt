package com.store.mgmt.modules.organization.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Store responses.
 */
public class StoreDTO {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String location;
    private String countryCode;
    private String contactInfo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StoreDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final StoreDTO dto = new StoreDTO();

        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder organizationId(UUID organizationId) { dto.organizationId = organizationId; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder location(String location) { dto.location = location; return this; }
        public Builder countryCode(String countryCode) { dto.countryCode = countryCode; return this; }
        public Builder contactInfo(String contactInfo) { dto.contactInfo = contactInfo; return this; }
        public Builder status(String status) { dto.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public StoreDTO build() { return dto; }
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

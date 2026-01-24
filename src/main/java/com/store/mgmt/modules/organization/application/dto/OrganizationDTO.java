package com.store.mgmt.modules.organization.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Organization responses.
 */
public class OrganizationDTO {

    private UUID id;
    private String name;
    private String description;
    private String contactInfo;
    private String appliedTemplateCode;
    private List<StoreDTO> stores;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrganizationDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final OrganizationDTO dto = new OrganizationDTO();

        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder contactInfo(String contactInfo) { dto.contactInfo = contactInfo; return this; }
        public Builder appliedTemplateCode(String appliedTemplateCode) { dto.appliedTemplateCode = appliedTemplateCode; return this; }
        public Builder stores(List<StoreDTO> stores) { dto.stores = stores; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public OrganizationDTO build() { return dto; }
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public String getAppliedTemplateCode() { return appliedTemplateCode; }
    public void setAppliedTemplateCode(String appliedTemplateCode) { this.appliedTemplateCode = appliedTemplateCode; }
    public List<StoreDTO> getStores() { return stores; }
    public void setStores(List<StoreDTO> stores) { this.stores = stores; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

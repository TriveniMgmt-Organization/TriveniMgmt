package com.store.mgmt.modules.products.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for ProductVariant responses.
 */
public class ProductVariantDTO {

    private UUID id;
    private UUID templateId;
    private String templateName;
    private String sku;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal retailPrice;
    private BigDecimal margin;
    private Map<String, String> attributeValues;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductVariantDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ProductVariantDTO dto = new ProductVariantDTO();

        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder templateId(UUID templateId) { dto.templateId = templateId; return this; }
        public Builder templateName(String templateName) { dto.templateName = templateName; return this; }
        public Builder sku(String sku) { dto.sku = sku; return this; }
        public Builder barcode(String barcode) { dto.barcode = barcode; return this; }
        public Builder costPrice(BigDecimal costPrice) { dto.costPrice = costPrice; return this; }
        public Builder retailPrice(BigDecimal retailPrice) { dto.retailPrice = retailPrice; return this; }
        public Builder margin(BigDecimal margin) { dto.margin = margin; return this; }
        public Builder attributeValues(Map<String, String> attributeValues) { dto.attributeValues = attributeValues; return this; }
        public Builder active(boolean active) { dto.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public ProductVariantDTO build() { return dto; }
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }
    public BigDecimal getMargin() { return margin; }
    public void setMargin(BigDecimal margin) { this.margin = margin; }
    public Map<String, String> getAttributeValues() { return attributeValues; }
    public void setAttributeValues(Map<String, String> attributeValues) { this.attributeValues = attributeValues; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

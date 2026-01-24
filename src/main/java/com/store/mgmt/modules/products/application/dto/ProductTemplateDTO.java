package com.store.mgmt.modules.products.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for ProductTemplate responses.
 */
public class ProductTemplateDTO {

    private UUID id;
    private String name;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private UUID brandId;
    private String brandName;
    private UUID unitOfMeasureId;
    private String unitOfMeasureName;
    private String imageUrl;
    private Integer reorderPoint;
    private boolean requiresExpiry;
    private boolean active;
    private Map<String, String> attributes;
    private List<ProductVariantDTO> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductTemplateDTO() {}

    // Builder pattern for cleaner construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ProductTemplateDTO dto = new ProductTemplateDTO();

        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder categoryId(UUID categoryId) { dto.categoryId = categoryId; return this; }
        public Builder categoryName(String categoryName) { dto.categoryName = categoryName; return this; }
        public Builder brandId(UUID brandId) { dto.brandId = brandId; return this; }
        public Builder brandName(String brandName) { dto.brandName = brandName; return this; }
        public Builder unitOfMeasureId(UUID unitOfMeasureId) { dto.unitOfMeasureId = unitOfMeasureId; return this; }
        public Builder unitOfMeasureName(String unitOfMeasureName) { dto.unitOfMeasureName = unitOfMeasureName; return this; }
        public Builder imageUrl(String imageUrl) { dto.imageUrl = imageUrl; return this; }
        public Builder reorderPoint(Integer reorderPoint) { dto.reorderPoint = reorderPoint; return this; }
        public Builder requiresExpiry(boolean requiresExpiry) { dto.requiresExpiry = requiresExpiry; return this; }
        public Builder active(boolean active) { dto.active = active; return this; }
        public Builder attributes(Map<String, String> attributes) { dto.attributes = attributes; return this; }
        public Builder variants(List<ProductVariantDTO> variants) { dto.variants = variants; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public ProductTemplateDTO build() { return dto; }
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public UUID getBrandId() { return brandId; }
    public void setBrandId(UUID brandId) { this.brandId = brandId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public UUID getUnitOfMeasureId() { return unitOfMeasureId; }
    public void setUnitOfMeasureId(UUID unitOfMeasureId) { this.unitOfMeasureId = unitOfMeasureId; }
    public String getUnitOfMeasureName() { return unitOfMeasureName; }
    public void setUnitOfMeasureName(String unitOfMeasureName) { this.unitOfMeasureName = unitOfMeasureName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    public boolean isRequiresExpiry() { return requiresExpiry; }
    public void setRequiresExpiry(boolean requiresExpiry) { this.requiresExpiry = requiresExpiry; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

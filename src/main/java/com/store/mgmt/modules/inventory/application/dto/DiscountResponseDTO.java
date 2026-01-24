package com.store.mgmt.modules.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for discount responses.
 */
public record DiscountResponseDTO(
        UUID id,
        UUID organizationId,
        UUID storeId,
        String storeName,
        String name,
        String type,
        BigDecimal value,
        LocalDate startDate,
        LocalDate endDate,
        UUID productTemplateId,
        String productTemplateName,
        UUID categoryId,
        String categoryName,
        String description,
        boolean isActive,
        boolean isCurrentlyValid,
        BigDecimal minimumPurchaseAmount,
        Integer minimumItemQuantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID organizationId;
        private UUID storeId;
        private String storeName;
        private String name;
        private String type;
        private BigDecimal value;
        private LocalDate startDate;
        private LocalDate endDate;
        private UUID productTemplateId;
        private String productTemplateName;
        private UUID categoryId;
        private String categoryName;
        private String description;
        private boolean isActive;
        private boolean isCurrentlyValid;
        private BigDecimal minimumPurchaseAmount;
        private Integer minimumItemQuantity;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder organizationId(UUID organizationId) { this.organizationId = organizationId; return this; }
        public Builder storeId(UUID storeId) { this.storeId = storeId; return this; }
        public Builder storeName(String storeName) { this.storeName = storeName; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder value(BigDecimal value) { this.value = value; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder productTemplateId(UUID productTemplateId) { this.productTemplateId = productTemplateId; return this; }
        public Builder productTemplateName(String productTemplateName) { this.productTemplateName = productTemplateName; return this; }
        public Builder categoryId(UUID categoryId) { this.categoryId = categoryId; return this; }
        public Builder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder isActive(boolean isActive) { this.isActive = isActive; return this; }
        public Builder isCurrentlyValid(boolean isCurrentlyValid) { this.isCurrentlyValid = isCurrentlyValid; return this; }
        public Builder minimumPurchaseAmount(BigDecimal minimumPurchaseAmount) { this.minimumPurchaseAmount = minimumPurchaseAmount; return this; }
        public Builder minimumItemQuantity(Integer minimumItemQuantity) { this.minimumItemQuantity = minimumItemQuantity; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public DiscountResponseDTO build() {
            return new DiscountResponseDTO(
                    id, organizationId, storeId, storeName, name, type, value,
                    startDate, endDate, productTemplateId, productTemplateName,
                    categoryId, categoryName, description, isActive, isCurrentlyValid,
                    minimumPurchaseAmount, minimumItemQuantity, createdAt, updatedAt
            );
        }
    }
}

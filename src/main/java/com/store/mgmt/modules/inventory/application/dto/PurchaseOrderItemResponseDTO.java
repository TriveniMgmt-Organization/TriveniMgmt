package com.store.mgmt.modules.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for purchase order item responses.
 */
public record PurchaseOrderItemResponseDTO(
        UUID id,
        UUID storeId,
        String storeName,
        UUID productTemplateId,
        String productTemplateName,
        UUID variantId,
        String variantSku,
        String variantName,
        int orderedQuantity,
        int receivedQuantity,
        int pendingQuantity,
        BigDecimal unitCost,
        BigDecimal totalCost
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID storeId;
        private String storeName;
        private UUID productTemplateId;
        private String productTemplateName;
        private UUID variantId;
        private String variantSku;
        private String variantName;
        private int orderedQuantity;
        private int receivedQuantity;
        private int pendingQuantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder storeId(UUID storeId) { this.storeId = storeId; return this; }
        public Builder storeName(String storeName) { this.storeName = storeName; return this; }
        public Builder productTemplateId(UUID productTemplateId) { this.productTemplateId = productTemplateId; return this; }
        public Builder productTemplateName(String productTemplateName) { this.productTemplateName = productTemplateName; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder orderedQuantity(int orderedQuantity) { this.orderedQuantity = orderedQuantity; return this; }
        public Builder receivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; return this; }
        public Builder pendingQuantity(int pendingQuantity) { this.pendingQuantity = pendingQuantity; return this; }
        public Builder unitCost(BigDecimal unitCost) { this.unitCost = unitCost; return this; }
        public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }

        public PurchaseOrderItemResponseDTO build() {
            return new PurchaseOrderItemResponseDTO(
                    id, storeId, storeName, productTemplateId, productTemplateName,
                    variantId, variantSku, variantName, orderedQuantity, receivedQuantity,
                    pendingQuantity, unitCost, totalCost
            );
        }
    }
}

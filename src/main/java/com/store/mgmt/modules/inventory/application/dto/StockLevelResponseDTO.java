package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for stock level responses.
 */
public record StockLevelResponseDTO(
        UUID id,
        UUID inventoryItemId,
        UUID variantId,
        String variantSku,
        String variantName,
        UUID locationId,
        String locationName,
        int onHand,
        int committed,
        int available,
        int lowStockThreshold,
        Integer maxStockLevel,
        boolean isLowStock,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID inventoryItemId;
        private UUID variantId;
        private String variantSku;
        private String variantName;
        private UUID locationId;
        private String locationName;
        private int onHand;
        private int committed;
        private int available;
        private int lowStockThreshold;
        private Integer maxStockLevel;
        private boolean isLowStock;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder inventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder locationId(UUID locationId) { this.locationId = locationId; return this; }
        public Builder locationName(String locationName) { this.locationName = locationName; return this; }
        public Builder onHand(int onHand) { this.onHand = onHand; return this; }
        public Builder committed(int committed) { this.committed = committed; return this; }
        public Builder available(int available) { this.available = available; return this; }
        public Builder lowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; return this; }
        public Builder maxStockLevel(Integer maxStockLevel) { this.maxStockLevel = maxStockLevel; return this; }
        public Builder isLowStock(boolean isLowStock) { this.isLowStock = isLowStock; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public StockLevelResponseDTO build() {
            return new StockLevelResponseDTO(
                    id, inventoryItemId, variantId, variantSku, variantName,
                    locationId, locationName, onHand, committed, available,
                    lowStockThreshold, maxStockLevel, isLowStock, updatedAt
            );
        }
    }
}

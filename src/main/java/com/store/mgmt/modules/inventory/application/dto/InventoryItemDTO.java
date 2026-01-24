package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for inventory item responses.
 */
public record InventoryItemDTO(
        UUID id,
        UUID variantId,
        UUID locationId,
        UUID storeId,
        String variantSku,
        String variantName,
        String locationName,
        int onHand,
        int reserved,
        int available,
        int reorderPoint,
        boolean isLowStock,
        String batchNumber,
        LocalDate expiryDate,
        boolean isExpiringSoon,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID variantId;
        private UUID locationId;
        private UUID storeId;
        private String variantSku;
        private String variantName;
        private String locationName;
        private int onHand;
        private int reserved;
        private int available;
        private int reorderPoint;
        private boolean isLowStock;
        private String batchNumber;
        private LocalDate expiryDate;
        private boolean isExpiringSoon;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder locationId(UUID locationId) { this.locationId = locationId; return this; }
        public Builder storeId(UUID storeId) { this.storeId = storeId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder locationName(String locationName) { this.locationName = locationName; return this; }
        public Builder onHand(int onHand) { this.onHand = onHand; return this; }
        public Builder reserved(int reserved) { this.reserved = reserved; return this; }
        public Builder available(int available) { this.available = available; return this; }
        public Builder reorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; return this; }
        public Builder isLowStock(boolean isLowStock) { this.isLowStock = isLowStock; return this; }
        public Builder batchNumber(String batchNumber) { this.batchNumber = batchNumber; return this; }
        public Builder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder isExpiringSoon(boolean isExpiringSoon) { this.isExpiringSoon = isExpiringSoon; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public InventoryItemDTO build() {
            return new InventoryItemDTO(
                    id, variantId, locationId, storeId, variantSku, variantName, locationName,
                    onHand, reserved, available, reorderPoint, isLowStock, batchNumber,
                    expiryDate, isExpiringSoon, createdAt, updatedAt
            );
        }
    }
}

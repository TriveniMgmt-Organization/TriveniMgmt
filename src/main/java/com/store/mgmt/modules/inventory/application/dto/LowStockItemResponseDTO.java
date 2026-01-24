package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for low stock item responses.
 */
public record LowStockItemResponseDTO(
        UUID inventoryItemId,
        UUID variantId,
        String variantSku,
        String variantName,
        UUID locationId,
        String locationName,
        int onHand,
        int available,
        int lowStockThreshold,
        int shortfall,
        LocalDate expiryDate
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID inventoryItemId;
        private UUID variantId;
        private String variantSku;
        private String variantName;
        private UUID locationId;
        private String locationName;
        private int onHand;
        private int available;
        private int lowStockThreshold;
        private int shortfall;
        private LocalDate expiryDate;

        public Builder inventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder locationId(UUID locationId) { this.locationId = locationId; return this; }
        public Builder locationName(String locationName) { this.locationName = locationName; return this; }
        public Builder onHand(int onHand) { this.onHand = onHand; return this; }
        public Builder available(int available) { this.available = available; return this; }
        public Builder lowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; return this; }
        public Builder shortfall(int shortfall) { this.shortfall = shortfall; return this; }
        public Builder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }

        public LowStockItemResponseDTO build() {
            return new LowStockItemResponseDTO(
                    inventoryItemId, variantId, variantSku, variantName,
                    locationId, locationName, onHand, available,
                    lowStockThreshold, shortfall, expiryDate
            );
        }
    }
}

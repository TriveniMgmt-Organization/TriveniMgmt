package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for stock transaction responses.
 */
public record StockTransactionResponseDTO(
        UUID id,
        UUID inventoryItemId,
        UUID variantId,
        String variantSku,
        String variantName,
        UUID locationId,
        String locationName,
        String type,
        int quantityDelta,
        String referenceType,
        UUID referenceId,
        String reason,
        UUID fromLocationId,
        String fromLocationName,
        UUID toLocationId,
        String toLocationName,
        UUID userId,
        String userName,
        LocalDateTime timestamp,
        String notes
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
        private String type;
        private int quantityDelta;
        private String referenceType;
        private UUID referenceId;
        private String reason;
        private UUID fromLocationId;
        private String fromLocationName;
        private UUID toLocationId;
        private String toLocationName;
        private UUID userId;
        private String userName;
        private LocalDateTime timestamp;
        private String notes;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder inventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder locationId(UUID locationId) { this.locationId = locationId; return this; }
        public Builder locationName(String locationName) { this.locationName = locationName; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder quantityDelta(int quantityDelta) { this.quantityDelta = quantityDelta; return this; }
        public Builder referenceType(String referenceType) { this.referenceType = referenceType; return this; }
        public Builder referenceId(UUID referenceId) { this.referenceId = referenceId; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder fromLocationId(UUID fromLocationId) { this.fromLocationId = fromLocationId; return this; }
        public Builder fromLocationName(String fromLocationName) { this.fromLocationName = fromLocationName; return this; }
        public Builder toLocationId(UUID toLocationId) { this.toLocationId = toLocationId; return this; }
        public Builder toLocationName(String toLocationName) { this.toLocationName = toLocationName; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public StockTransactionResponseDTO build() {
            return new StockTransactionResponseDTO(
                    id, inventoryItemId, variantId, variantSku, variantName,
                    locationId, locationName, type, quantityDelta,
                    referenceType, referenceId, reason,
                    fromLocationId, fromLocationName, toLocationId, toLocationName,
                    userId, userName, timestamp, notes
            );
        }
    }
}

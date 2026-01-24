package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for damage/loss responses.
 */
public record DamageLossResponseDTO(
        UUID id,
        UUID organizationId,
        UUID storeId,
        String storeName,
        UUID variantId,
        String variantSku,
        String variantName,
        UUID locationId,
        String locationName,
        int quantity,
        String reason,
        LocalDateTime dateRecorded,
        String notes,
        UUID userId,
        String userName,
        LocalDateTime createdAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID organizationId;
        private UUID storeId;
        private String storeName;
        private UUID variantId;
        private String variantSku;
        private String variantName;
        private UUID locationId;
        private String locationName;
        private int quantity;
        private String reason;
        private LocalDateTime dateRecorded;
        private String notes;
        private UUID userId;
        private String userName;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder organizationId(UUID organizationId) { this.organizationId = organizationId; return this; }
        public Builder storeId(UUID storeId) { this.storeId = storeId; return this; }
        public Builder storeName(String storeName) { this.storeName = storeName; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder locationId(UUID locationId) { this.locationId = locationId; return this; }
        public Builder locationName(String locationName) { this.locationName = locationName; return this; }
        public Builder quantity(int quantity) { this.quantity = quantity; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder dateRecorded(LocalDateTime dateRecorded) { this.dateRecorded = dateRecorded; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DamageLossResponseDTO build() {
            return new DamageLossResponseDTO(
                    id, organizationId, storeId, storeName,
                    variantId, variantSku, variantName,
                    locationId, locationName, quantity, reason,
                    dateRecorded, notes, userId, userName, createdAt
            );
        }
    }
}

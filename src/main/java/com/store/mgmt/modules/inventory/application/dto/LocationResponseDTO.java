package com.store.mgmt.modules.inventory.application.dto;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for inventory location responses.
 */
public record LocationResponseDTO(
        UUID id,
        UUID storeId,
        String storeName,
        String name,
        String address,
        InventoryLocationType type,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID storeId;
        private String storeName;
        private String name;
        private String address;
        private InventoryLocationType type;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder storeId(UUID storeId) { this.storeId = storeId; return this; }
        public Builder storeName(String storeName) { this.storeName = storeName; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder type(InventoryLocationType type) { this.type = type; return this; }
        public Builder isActive(boolean isActive) { this.isActive = isActive; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public LocationResponseDTO build() {
            return new LocationResponseDTO(id, storeId, storeName, name, address, type, isActive, createdAt, updatedAt);
        }
    }
}

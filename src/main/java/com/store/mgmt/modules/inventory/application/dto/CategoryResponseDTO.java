package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for category responses.
 */
public record CategoryResponseDTO(
        UUID id,
        UUID organizationId,
        String code,
        String name,
        String description,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID organizationId;
        private String code;
        private String name;
        private String description;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder organizationId(UUID organizationId) { this.organizationId = organizationId; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder isActive(boolean isActive) { this.isActive = isActive; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public CategoryResponseDTO build() {
            return new CategoryResponseDTO(id, organizationId, code, name, description, isActive, createdAt, updatedAt);
        }
    }
}

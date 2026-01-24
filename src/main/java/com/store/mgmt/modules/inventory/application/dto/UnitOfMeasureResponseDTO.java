package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for unit of measure responses.
 */
public record UnitOfMeasureResponseDTO(
        UUID id,
        UUID organizationId,
        String name,
        String code,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID organizationId;
        private String name;
        private String code;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder organizationId(UUID organizationId) { this.organizationId = organizationId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UnitOfMeasureResponseDTO build() {
            return new UnitOfMeasureResponseDTO(id, organizationId, name, code, createdAt, updatedAt);
        }
    }
}

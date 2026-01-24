package com.store.mgmt.modules.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for UoM conversion responses.
 */
public record UoMConversionResponseDTO(
        UUID id,
        UUID fromUomId,
        String fromUomName,
        String fromUomCode,
        UUID toUomId,
        String toUomName,
        String toUomCode,
        BigDecimal ratio,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID fromUomId;
        private String fromUomName;
        private String fromUomCode;
        private UUID toUomId;
        private String toUomName;
        private String toUomCode;
        private BigDecimal ratio;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder fromUomId(UUID fromUomId) { this.fromUomId = fromUomId; return this; }
        public Builder fromUomName(String fromUomName) { this.fromUomName = fromUomName; return this; }
        public Builder fromUomCode(String fromUomCode) { this.fromUomCode = fromUomCode; return this; }
        public Builder toUomId(UUID toUomId) { this.toUomId = toUomId; return this; }
        public Builder toUomName(String toUomName) { this.toUomName = toUomName; return this; }
        public Builder toUomCode(String toUomCode) { this.toUomCode = toUomCode; return this; }
        public Builder ratio(BigDecimal ratio) { this.ratio = ratio; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UoMConversionResponseDTO build() {
            return new UoMConversionResponseDTO(
                    id, fromUomId, fromUomName, fromUomCode,
                    toUomId, toUomName, toUomCode,
                    ratio, description, createdAt, updatedAt
            );
        }
    }
}

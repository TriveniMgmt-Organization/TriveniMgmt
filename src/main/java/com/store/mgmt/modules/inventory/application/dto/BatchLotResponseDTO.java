package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for batch/lot responses.
 */
public record BatchLotResponseDTO(
        UUID id,
        String batchNumber,
        LocalDate manufactureDate,
        LocalDate expiryDate,
        UUID supplierId,
        String supplierName,
        boolean isActive,
        boolean isExpired,
        boolean isExpiringSoon,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String batchNumber;
        private LocalDate manufactureDate;
        private LocalDate expiryDate;
        private UUID supplierId;
        private String supplierName;
        private boolean isActive;
        private boolean isExpired;
        private boolean isExpiringSoon;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder batchNumber(String batchNumber) { this.batchNumber = batchNumber; return this; }
        public Builder manufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; return this; }
        public Builder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder supplierId(UUID supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder isActive(boolean isActive) { this.isActive = isActive; return this; }
        public Builder isExpired(boolean isExpired) { this.isExpired = isExpired; return this; }
        public Builder isExpiringSoon(boolean isExpiringSoon) { this.isExpiringSoon = isExpiringSoon; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public BatchLotResponseDTO build() {
            return new BatchLotResponseDTO(
                    id, batchNumber, manufactureDate, expiryDate,
                    supplierId, supplierName, isActive, isExpired, isExpiringSoon,
                    createdAt, updatedAt
            );
        }
    }
}

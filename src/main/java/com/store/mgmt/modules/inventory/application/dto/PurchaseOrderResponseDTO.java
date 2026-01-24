package com.store.mgmt.modules.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for purchase order responses.
 */
public record PurchaseOrderResponseDTO(
        UUID id,
        UUID organizationId,
        UUID supplierId,
        String supplierName,
        LocalDateTime orderDate,
        LocalDate expectedDeliveryDate,
        LocalDate actualDeliveryDate,
        String status,
        BigDecimal totalEstimatedAmount,
        String trackingNumber,
        String notes,
        UUID userId,
        String userName,
        List<PurchaseOrderItemResponseDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID organizationId;
        private UUID supplierId;
        private String supplierName;
        private LocalDateTime orderDate;
        private LocalDate expectedDeliveryDate;
        private LocalDate actualDeliveryDate;
        private String status;
        private BigDecimal totalEstimatedAmount;
        private String trackingNumber;
        private String notes;
        private UUID userId;
        private String userName;
        private List<PurchaseOrderItemResponseDTO> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder organizationId(UUID organizationId) { this.organizationId = organizationId; return this; }
        public Builder supplierId(UUID supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder orderDate(LocalDateTime orderDate) { this.orderDate = orderDate; return this; }
        public Builder expectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; return this; }
        public Builder actualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder totalEstimatedAmount(BigDecimal totalEstimatedAmount) { this.totalEstimatedAmount = totalEstimatedAmount; return this; }
        public Builder trackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder items(List<PurchaseOrderItemResponseDTO> items) { this.items = items; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PurchaseOrderResponseDTO build() {
            return new PurchaseOrderResponseDTO(
                    id, organizationId, supplierId, supplierName, orderDate,
                    expectedDeliveryDate, actualDeliveryDate, status, totalEstimatedAmount,
                    trackingNumber, notes, userId, userName, items, createdAt, updatedAt
            );
        }
    }
}

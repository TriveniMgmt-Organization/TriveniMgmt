package com.store.mgmt.modules.inventory.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Sale.
 */
@Builder
public record SaleResponseDTO(
        UUID id,
        UUID storeId,
        String storeName,
        LocalDateTime saleTimestamp,
        BigDecimal totalAmount,
        BigDecimal totalDiscountAmount,
        String paymentMethod,
        String transactionId,
        UUID userId,
        String userName,
        String notes,
        List<SaleItemResponseDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

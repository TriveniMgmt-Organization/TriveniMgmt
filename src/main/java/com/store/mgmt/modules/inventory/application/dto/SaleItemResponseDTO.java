package com.store.mgmt.modules.inventory.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for SaleItem.
 */
@Builder
public record SaleItemResponseDTO(
        UUID id,
        UUID productTemplateId,
        String productTemplateName,
        UUID variantId,
        String variantSku,
        String variantName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal
) {}

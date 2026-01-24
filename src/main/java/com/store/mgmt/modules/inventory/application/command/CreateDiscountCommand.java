package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to create a new discount.
 */
public record CreateDiscountCommand(
        UUID organizationId,
        UUID storeId,
        String name,
        String type,
        BigDecimal value,
        LocalDate startDate,
        LocalDate endDate,
        UUID productTemplateId,
        UUID categoryId,
        String description,
        BigDecimal minimumPurchaseAmount,
        Integer minimumItemQuantity,
        Boolean isActive
) implements Command<DiscountResponseDTO> {}

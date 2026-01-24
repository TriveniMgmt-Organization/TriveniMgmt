package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.CreateSaleItemRequestDTO;
import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.List;
import java.util.UUID;

/**
 * Command to process a sale.
 */
public record ProcessSaleCommand(
        UUID storeId,
        String paymentMethod,
        String transactionId,
        String notes,
        List<CreateSaleItemRequestDTO> items,
        UUID userId
) implements Command<SaleResponseDTO> {}

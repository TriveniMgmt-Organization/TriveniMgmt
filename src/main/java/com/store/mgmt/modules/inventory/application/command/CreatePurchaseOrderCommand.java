package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.CreatePurchaseOrderItemRequestDTO;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Command to create a new purchase order.
 */
public record CreatePurchaseOrderCommand(
        UUID organizationId,
        UUID supplierId,
        LocalDate expectedDeliveryDate,
        String trackingNumber,
        String notes,
        List<CreatePurchaseOrderItemRequestDTO> items,
        UUID userId
) implements Command<PurchaseOrderResponseDTO> {}

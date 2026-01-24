package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to update an existing purchase order.
 */
public record UpdatePurchaseOrderCommand(
        UUID id,
        UUID organizationId,
        LocalDate expectedDeliveryDate,
        String trackingNumber,
        String notes,
        String status
) implements Command<PurchaseOrderResponseDTO> {}

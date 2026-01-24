package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.modules.inventory.application.dto.ReceivePurchaseOrderItemRequestDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.List;
import java.util.UUID;

/**
 * Command to receive items for a purchase order.
 * This creates BatchLots, InventoryItems, and StockTransactions.
 */
public record ReceivePurchaseOrderCommand(
        UUID purchaseOrderId,
        UUID organizationId,
        List<ReceivePurchaseOrderItemRequestDTO> items,
        UUID userId
) implements Command<PurchaseOrderResponseDTO> {}

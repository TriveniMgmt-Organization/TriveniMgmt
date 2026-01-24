package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.*;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.modules.inventory.application.dto.ReceivePurchaseOrderItemRequestDTO;
import com.store.mgmt.modules.inventory.application.service.PurchaseOrderMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler for ReceivePurchaseOrderCommand.
 * Processes receipt of PO items - creates BatchLots, InventoryItems, and StockTransactions.
 */
@Component
@Transactional
public class ReceivePurchaseOrderHandler implements CommandHandler<ReceivePurchaseOrderCommand, PurchaseOrderResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(ReceivePurchaseOrderHandler.class);

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderItemRepository poItemRepository;
    private final BatchLotRepository batchLotRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryLocationRepository locationRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockTransactionRepository transactionRepository;
    private final PurchaseOrderMapper mapper;

    public ReceivePurchaseOrderHandler(
            PurchaseOrderRepository poRepository,
            PurchaseOrderItemRepository poItemRepository,
            BatchLotRepository batchLotRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryLocationRepository locationRepository,
            StockLevelRepository stockLevelRepository,
            StockTransactionRepository transactionRepository,
            PurchaseOrderMapper mapper
    ) {
        this.poRepository = poRepository;
        this.poItemRepository = poItemRepository;
        this.batchLotRepository = batchLotRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.locationRepository = locationRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @Override
    public PurchaseOrderResponseDTO handle(ReceivePurchaseOrderCommand cmd) {
        log.debug("Receiving items for purchase order: {}", cmd.purchaseOrderId());

        PurchaseOrder po = poRepository.findByIdAndOrganizationId(cmd.purchaseOrderId(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with ID: " + cmd.purchaseOrderId()));

        // Validate PO status
        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot receive items for a cancelled purchase order");
        }
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED_COMPLETE) {
            throw new IllegalArgumentException("Purchase order is already fully received");
        }

        // Process each received item
        for (ReceivePurchaseOrderItemRequestDTO itemDto : cmd.items()) {
            processReceivedItem(po, itemDto, cmd.userId());
        }

        // Update PO status based on received quantities
        updatePurchaseOrderStatus(po);

        PurchaseOrder saved = poRepository.save(po);
        log.info("Processed receipt for purchase order with ID: {}, new status: {}",
                saved.getId(), saved.getStatus());

        return mapper.toResponseDTO(saved);
    }

    private void processReceivedItem(PurchaseOrder po, ReceivePurchaseOrderItemRequestDTO itemDto, UUID userId) {
        // Find the PO item
        PurchaseOrderItem poItem = poItemRepository.findById(itemDto.poItemId())
                .orElseThrow(() -> new EntityNotFoundException("PO item not found with ID: " + itemDto.poItemId()));

        // Validate the PO item belongs to this PO
        if (!poItem.getPurchaseOrder().getId().equals(po.getId())) {
            throw new IllegalArgumentException("PO item does not belong to this purchase order");
        }

        // Validate received quantity
        int remainingToReceive = poItem.getOrderedQuantity() - poItem.getReceivedQuantity();
        if (itemDto.receivedQuantity() > remainingToReceive) {
            throw new IllegalArgumentException(
                    "Received quantity (" + itemDto.receivedQuantity() +
                    ") exceeds remaining quantity (" + remainingToReceive + ") for item"
            );
        }

        // Validate location
        InventoryLocation location = locationRepository.findById(itemDto.locationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + itemDto.locationId()));

        // Handle batch/lot if provided
        BatchLot batchLot = null;
        if (itemDto.batchNumber() != null && !itemDto.batchNumber().isEmpty()) {
            batchLot = batchLotRepository.findByBatchNumber(itemDto.batchNumber())
                    .orElseGet(() -> {
                        BatchLot newBatch = new BatchLot();
                        newBatch.setBatchNumber(itemDto.batchNumber());
                        newBatch.setExpiryDate(itemDto.expiryDate());
                        newBatch.setSupplier(po.getSupplier());
                        newBatch.setActive(true);
                        return batchLotRepository.save(newBatch);
                    });
        }

        // Find or create inventory item
        InventoryItem inventoryItem = findOrCreateInventoryItem(
                poItem.getVariant(), location, batchLot, itemDto.expiryDate()
        );

        // Create stock transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setInventoryItem(inventoryItem);
        transaction.setType(TransactionType.RECEIPT);
        transaction.setQuantityDelta(itemDto.receivedQuantity());
        transaction.setReferenceType("PURCHASE_ORDER");
        transaction.setReferenceId(po.getId());
        transaction.setUserId(userId);
        transactionRepository.save(transaction);

        // Update stock level
        StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(inventoryItem.getId())
                .orElseGet(() -> {
                    StockLevel newLevel = new StockLevel();
                    newLevel.setInventoryItem(inventoryItem);
                    newLevel.setOnHand(0);
                    newLevel.setCommitted(0);
                    newLevel.setAvailable(0);
                    return newLevel;
                });
        stockLevel.setOnHand(stockLevel.getOnHand() + itemDto.receivedQuantity());
        stockLevelRepository.save(stockLevel);

        // Update PO item received quantity
        poItem.setReceivedQuantity(poItem.getReceivedQuantity() + itemDto.receivedQuantity());
        poItemRepository.save(poItem);

        log.debug("Received {} units of variant {} at location {}",
                itemDto.receivedQuantity(), poItem.getVariant().getId(), location.getId());
    }

    private InventoryItem findOrCreateInventoryItem(
            ProductVariant variant,
            InventoryLocation location,
            BatchLot batchLot,
            LocalDate expiryDate
    ) {
        // Try to find existing inventory item with same variant, location, and batch
        Optional<InventoryItem> existing;
        if (batchLot != null) {
            existing = inventoryItemRepository.findByVariantIdAndLocationIdAndBatchLotId(
                    variant.getId(), location.getId(), batchLot.getId()
            );
        } else {
            existing = inventoryItemRepository.findByVariantIdAndLocationIdAndBatchLotId(
                    variant.getId(), location.getId(), null
            );
        }

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new inventory item
        InventoryItem newItem = new InventoryItem();
        newItem.setVariant(variant);
        newItem.setLocation(location);
        newItem.setBatchLot(batchLot);
        newItem.setExpiryDate(expiryDate != null ? expiryDate : (batchLot != null ? batchLot.getExpiryDate() : null));
        return inventoryItemRepository.save(newItem);
    }

    private void updatePurchaseOrderStatus(PurchaseOrder po) {
        boolean allFullyReceived = true;
        boolean anyReceived = false;

        for (PurchaseOrderItem item : po.getPurchaseOrderItems()) {
            if (item.getReceivedQuantity() > 0) {
                anyReceived = true;
            }
            if (item.getReceivedQuantity() < item.getOrderedQuantity()) {
                allFullyReceived = false;
            }
        }

        if (allFullyReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED_COMPLETE);
            po.setActualDeliveryDate(LocalDate.now());
        } else if (anyReceived) {
            po.setStatus(PurchaseOrderStatus.RECEIVED_PARTIAL);
        }
    }
}

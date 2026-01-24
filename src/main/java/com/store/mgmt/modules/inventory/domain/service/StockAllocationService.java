package com.store.mgmt.modules.inventory.domain.service;

import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.model.StockTransaction;
import com.store.mgmt.modules.inventory.domain.model.TransactionType;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for stock allocation using FIFO strategy with expiry date sorting.
 */
@Service
public class StockAllocationService {

    private static final Logger log = LoggerFactory.getLogger(StockAllocationService.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public StockAllocationService(
            InventoryItemRepository inventoryItemRepository,
            StockLevelRepository stockLevelRepository,
            StockTransactionRepository stockTransactionRepository
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    /**
     * Allocate stock for a variant using FIFO strategy.
     * Items with earlier expiry dates are allocated first.
     * If no expiry date, falls back to creation date.
     *
     * @param variantId      The variant to allocate stock for
     * @param storeId        The store context
     * @param quantityNeeded The quantity to allocate
     * @param reference      Reference for the transaction (e.g., sale ID)
     * @return List of stock transactions created
     * @throws InsufficientStockException if there's not enough stock
     */
    public List<StockTransaction> allocateStock(
            UUID variantId,
            UUID storeId,
            int quantityNeeded,
            String reference
    ) {
        log.debug("Allocating {} units for variant {} in store {}", quantityNeeded, variantId, storeId);

        List<InventoryItem> availableItems = getAvailableInventoryItems(variantId, storeId);
        int totalAvailable = calculateTotalAvailable(availableItems);

        if (totalAvailable < quantityNeeded) {
            throw new IllegalStateException(
                    "Insufficient stock for variant " + variantId +
                            ". Requested: " + quantityNeeded + ", Available: " + totalAvailable
            );
        }

        List<StockTransaction> transactions = new ArrayList<>();
        int remainingToAllocate = quantityNeeded;

        for (InventoryItem item : availableItems) {
            if (remainingToAllocate <= 0) break;

            StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(item.getId())
                    .orElse(null);

            if (stockLevel == null || stockLevel.getAvailable() <= 0) {
                continue;
            }

            int quantityToTake = Math.min(remainingToAllocate, stockLevel.getAvailable());

            // Update stock level
            stockLevel.setAvailable(stockLevel.getAvailable() - quantityToTake);
            stockLevelRepository.save(stockLevel);

            // Create stock transaction
            StockTransaction transaction = new StockTransaction();
            transaction.setInventoryItem(item);
            transaction.setQuantityDelta(-quantityToTake);
            transaction.setType(TransactionType.SALE);
            transaction.setReferenceType("SALE");
            transaction.setNotes(reference);
            transactions.add(stockTransactionRepository.save(transaction));

            remainingToAllocate -= quantityToTake;
            log.debug("Allocated {} units from inventory item {}", quantityToTake, item.getId());
        }

        if (remainingToAllocate > 0) {
            throw new IllegalStateException(
                    "Failed to fully allocate stock for variant " + variantId +
                            ". Remaining: " + remainingToAllocate
            );
        }

        return transactions;
    }

    /**
     * Check if sufficient stock is available for a variant.
     */
    public boolean hasEnoughStock(UUID variantId, UUID storeId, int quantityNeeded) {
        List<InventoryItem> availableItems = getAvailableInventoryItems(variantId, storeId);
        int totalAvailable = calculateTotalAvailable(availableItems);
        return totalAvailable >= quantityNeeded;
    }

    /**
     * Get total available stock for a variant.
     */
    public int getTotalAvailableStock(UUID variantId, UUID storeId) {
        List<InventoryItem> availableItems = getAvailableInventoryItems(variantId, storeId);
        return calculateTotalAvailable(availableItems);
    }

    /**
     * Get available inventory items sorted by FIFO (expiry date, then creation date).
     */
    private List<InventoryItem> getAvailableInventoryItems(UUID variantId, UUID storeId) {
        List<InventoryItem> items = inventoryItemRepository.findByVariantIdAndStoreId(variantId, storeId);

        return items.stream()
                .filter(item -> {
                    StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(item.getId()).orElse(null);
                    return stockLevel != null && stockLevel.getAvailable() > 0;
                })
                .sorted(fifoComparator())
                .toList();
    }

    /**
     * FIFO comparator: earlier expiry dates first, then earlier creation dates.
     */
    private Comparator<InventoryItem> fifoComparator() {
        return (item1, item2) -> {
            // Items with expiry dates come before items without
            if (item1.getExpiryDate() != null && item2.getExpiryDate() != null) {
                return item1.getExpiryDate().compareTo(item2.getExpiryDate());
            }
            if (item1.getExpiryDate() != null) return -1;
            if (item2.getExpiryDate() != null) return 1;
            // Fallback to creation date (earlier first)
            return item1.getCreatedAt().compareTo(item2.getCreatedAt());
        };
    }

    private int calculateTotalAvailable(List<InventoryItem> items) {
        return items.stream()
                .mapToInt(item -> {
                    StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(item.getId()).orElse(null);
                    return stockLevel != null ? stockLevel.getAvailable() : 0;
                })
                .sum();
    }
}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.domain.exception.ResourceNotFoundException;
import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.model.StockTransaction;
import com.store.mgmt.modules.inventory.domain.model.TransactionType;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockTransactionRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Handler for IssueStockCommand.
 */
@Component
@Transactional
public class IssueStockHandler implements CommandHandler<IssueStockCommand, InventoryItemDTO> {

    private static final Logger log = LoggerFactory.getLogger(IssueStockHandler.class);

    private final InventoryItemRepository inventoryRepo;
    private final StockLevelRepository stockLevelRepository;
    private final StockTransactionRepository transactionRepository;

    public IssueStockHandler(
            InventoryItemRepository inventoryRepo,
            StockLevelRepository stockLevelRepository,
            StockTransactionRepository transactionRepository
    ) {
        this.inventoryRepo = inventoryRepo;
        this.stockLevelRepository = stockLevelRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public InventoryItemDTO handle(IssueStockCommand cmd) {
        log.debug("Issuing stock: item={}, quantity={}", cmd.itemId(), cmd.quantity());

        TenantContext tenant = TenantContext.current();
        tenant.requireStore(cmd.storeId());

        InventoryItem item = inventoryRepo.findByIdAndStoreId(cmd.itemId(), cmd.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem not found: " + cmd.itemId()));

        // Update stock level
        StockLevel stockLevel = item.getStockLevel();
        if (stockLevel == null) {
            throw new IllegalStateException("Inventory item has no stock level: " + cmd.itemId());
        }

        // Check available stock
        if (stockLevel.getAvailable() < cmd.quantity()) {
            throw new IllegalStateException(
                    "Cannot issue " + cmd.quantity() + " units. Available: " + stockLevel.getAvailable());
        }

        stockLevel.setOnHand(stockLevel.getOnHand() - cmd.quantity());
        stockLevelRepository.save(stockLevel);

        // Create transaction record
        StockTransaction transaction = new StockTransaction();
        transaction.setInventoryItem(item);
        transaction.setType(TransactionType.SALE);
        transaction.setQuantityDelta(-cmd.quantity());
        transaction.setNotes(cmd.reason());
        transactionRepository.save(transaction);

        log.info("Issued {} units from item {}", cmd.quantity(), cmd.itemId());

        return toDTO(item);
    }

    private InventoryItemDTO toDTO(InventoryItem item) {
        StockLevel stockLevel = item.getStockLevel();
        boolean isLowStock = stockLevel != null && stockLevel.getAvailable() <= stockLevel.getLowStockThreshold();
        boolean isExpiringSoon = item.getExpiryDate() != null &&
                item.getExpiryDate().isBefore(LocalDate.now().plusDays(30));

        return InventoryItemDTO.builder()
                .id(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .locationId(item.getLocation() != null ? item.getLocation().getId() : null)
                .storeId(item.getLocation() != null ? item.getLocation().getStoreId() : null)
                .onHand(stockLevel != null ? stockLevel.getOnHand() : 0)
                .reserved(stockLevel != null ? stockLevel.getCommitted() : 0)
                .available(stockLevel != null ? stockLevel.getAvailable() : 0)
                .reorderPoint(stockLevel != null ? stockLevel.getLowStockThreshold() : 0)
                .isLowStock(isLowStock)
                .batchNumber(item.getBatchLot() != null ? item.getBatchLot().getBatchNumber() : null)
                .expiryDate(item.getExpiryDate())
                .isExpiringSoon(isExpiringSoon)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.model.StockTransaction;
import com.store.mgmt.modules.inventory.domain.model.AdjustmentReason;
import com.store.mgmt.modules.inventory.domain.model.TransactionType;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.modules.inventory.domain.repository.InventoryLocationRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockTransactionRepository;
import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import com.store.mgmt.modules.inventory.application.service.StockTransactionMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateStockTransactionCommand.
 */
@Component
@Transactional
public class CreateStockTransactionHandler implements CommandHandler<CreateStockTransactionCommand, StockTransactionResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateStockTransactionHandler.class);

    private final StockTransactionRepository transactionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockLevelRepository stockLevelRepository;
    private final InventoryLocationRepository locationRepository;
    private final StockTransactionMapper mapper;

    public CreateStockTransactionHandler(
            StockTransactionRepository transactionRepository,
            InventoryItemRepository inventoryItemRepository,
            StockLevelRepository stockLevelRepository,
            InventoryLocationRepository locationRepository,
            StockTransactionMapper mapper
    ) {
        this.transactionRepository = transactionRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.locationRepository = locationRepository;
        this.mapper = mapper;
    }

    @Override
    public StockTransactionResponseDTO handle(CreateStockTransactionCommand cmd) {
        log.debug("Creating stock transaction for inventory item: {}, type: {}, delta: {}",
                cmd.inventoryItemId(), cmd.type(), cmd.quantityDelta());

        // Validate quantity delta is not zero
        if (cmd.quantityDelta() == 0) {
            throw new IllegalArgumentException("Quantity delta cannot be zero");
        }

        // Find inventory item
        InventoryItem inventoryItem = inventoryItemRepository.findById(cmd.inventoryItemId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with ID: " + cmd.inventoryItemId()));

        // Parse transaction type
        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(cmd.type());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + cmd.type());
        }

        // For negative deltas (sales, damage, etc.), validate available stock
        if (cmd.quantityDelta() < 0) {
            StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(cmd.inventoryItemId())
                    .orElse(null);
            int availableStock = stockLevel != null ? stockLevel.getAvailable() : 0;
            if (availableStock < Math.abs(cmd.quantityDelta())) {
                throw new IllegalArgumentException(
                        "Insufficient stock. Available: " + availableStock + ", requested: " + Math.abs(cmd.quantityDelta())
                );
            }
        }

        // Create transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setInventoryItem(inventoryItem);
        transaction.setType(transactionType);
        transaction.setQuantityDelta(cmd.quantityDelta());
        transaction.setReferenceType(cmd.referenceType());
        transaction.setReferenceId(cmd.referenceId());
        transaction.setNotes(cmd.notes());

        // Set adjustment reason if provided
        if (cmd.reason() != null && !cmd.reason().isEmpty()) {
            try {
                transaction.setReason(AdjustmentReason.valueOf(cmd.reason()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid adjustment reason: {}", cmd.reason());
            }
        }

        // Set from/to locations for transfers
        if (cmd.fromLocationId() != null) {
            InventoryLocation fromLocation = locationRepository.findById(cmd.fromLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("From location not found with ID: " + cmd.fromLocationId()));
            transaction.setFromLocation(fromLocation);
        }
        if (cmd.toLocationId() != null) {
            InventoryLocation toLocation = locationRepository.findById(cmd.toLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("To location not found with ID: " + cmd.toLocationId()));
            transaction.setToLocation(toLocation);
        }

        // Set user if provided
        if (cmd.userId() != null) {
            transaction.setUserId(cmd.userId());
        }

        // Save transaction
        StockTransaction saved = transactionRepository.save(transaction);

        // Update stock level
        updateStockLevel(inventoryItem, cmd.quantityDelta());

        log.info("Created stock transaction with ID: {}, type: {}, delta: {}",
                saved.getId(), transactionType, cmd.quantityDelta());

        return mapper.toResponseDTO(saved);
    }

    private void updateStockLevel(InventoryItem inventoryItem, int quantityDelta) {
        StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(inventoryItem.getId())
                .orElseGet(() -> {
                    StockLevel newLevel = new StockLevel();
                    newLevel.setInventoryItem(inventoryItem);
                    newLevel.setOnHand(0);
                    newLevel.setCommitted(0);
                    newLevel.setAvailable(0);
                    return newLevel;
                });

        stockLevel.setOnHand(stockLevel.getOnHand() + quantityDelta);
        // Available is auto-calculated in @PreUpdate/@PrePersist
        stockLevelRepository.save(stockLevel);
    }
}

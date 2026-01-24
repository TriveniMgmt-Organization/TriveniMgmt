package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.domain.exception.ResourceNotFoundException;
import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.domain.model.ProductVariant;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.modules.inventory.domain.repository.InventoryLocationRepository;
import com.store.mgmt.modules.inventory.domain.repository.ProductVariantRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.domain.exception.DuplicateEntityException;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Handler for CreateInventoryItemCommand.
 */
@Component
@Transactional
public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItemCommand, InventoryItemDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateInventoryItemHandler.class);

    private final InventoryItemRepository inventoryRepo;
    private final ProductVariantRepository variantRepository;
    private final InventoryLocationRepository locationRepository;
    private final StockLevelRepository stockLevelRepository;

    public CreateInventoryItemHandler(
            InventoryItemRepository inventoryRepo,
            ProductVariantRepository variantRepository,
            InventoryLocationRepository locationRepository,
            StockLevelRepository stockLevelRepository
    ) {
        this.inventoryRepo = inventoryRepo;
        this.variantRepository = variantRepository;
        this.locationRepository = locationRepository;
        this.stockLevelRepository = stockLevelRepository;
    }

    @Override
    public InventoryItemDTO handle(CreateInventoryItemCommand cmd) {
        log.debug("Creating inventory item: variant={}, location={}", cmd.variantId(), cmd.locationId());

        // Validate tenant context
        TenantContext tenant = TenantContext.current();
        tenant.requireStore(cmd.storeId());

        // Check for duplicate
        if (inventoryRepo.findByVariantIdAndLocationIdAndBatchLotId(
                cmd.variantId(), cmd.locationId(), null).isPresent()) {
            throw new DuplicateEntityException(
                    "InventoryItem",
                    String.format("variant=%s, location=%s", cmd.variantId(), cmd.locationId())
            );
        }

        // Fetch related entities
        ProductVariant variant = variantRepository.findById(cmd.variantId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant not found: " + cmd.variantId()));

        InventoryLocation location = locationRepository.findById(cmd.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryLocation not found: " + cmd.locationId()));

        // Create inventory item
        InventoryItem item = new InventoryItem();
        item.setVariant(variant);
        item.setLocation(location);
        item.setExpiryDate(cmd.expiryDate());

        // Save inventory item first
        InventoryItem saved = inventoryRepo.save(item);

        // Create stock level
        StockLevel stockLevel = new StockLevel();
        stockLevel.setInventoryItem(saved);
        stockLevel.setOnHand(cmd.initialQuantity());
        stockLevel.setCommitted(0);
        stockLevel.setAvailable(cmd.initialQuantity());
        stockLevel.setLowStockThreshold(cmd.lowStockThreshold());
        stockLevelRepository.save(stockLevel);

        // Refresh to get the stock level
        saved.setStockLevel(stockLevel);

        log.info("Created inventory item: {}", saved.getId());

        return toDTO(saved);
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

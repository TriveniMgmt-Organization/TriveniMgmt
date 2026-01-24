package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetInventoryItemsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetInventoryItemsHandler implements QueryHandler<GetInventoryItemsQuery, List<InventoryItemDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetInventoryItemsHandler.class);

    private final InventoryItemRepository inventoryRepo;
    private final StockLevelRepository stockLevelRepository;

    public GetInventoryItemsHandler(InventoryItemRepository inventoryRepo, StockLevelRepository stockLevelRepository) {
        this.inventoryRepo = inventoryRepo;
        this.stockLevelRepository = stockLevelRepository;
    }

    @Override
    public List<InventoryItemDTO> handle(GetInventoryItemsQuery query) {
        log.debug("Getting inventory items for store: {}", query.storeId());

        TenantContext tenant = TenantContext.current();
        tenant.requireStore(query.storeId());

        List<InventoryItem> items;

        if (query.lowStockOnly()) {
            // Get low stock items by finding items where available <= lowStockThreshold
            List<StockLevel> lowStockLevels = stockLevelRepository.findLowStockItemsByStoreId(query.storeId());
            items = lowStockLevels.stream()
                    .map(StockLevel::getInventoryItem)
                    .collect(Collectors.toList());
        } else if (query.expiringSoonOnly()) {
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(30);
            items = inventoryRepo.findExpiringBetween(today, futureDate);
        } else if (query.locationId() != null) {
            items = inventoryRepo.findByLocationId(query.locationId());
        } else {
            items = inventoryRepo.findByStoreId(query.storeId());
        }

        // Simple pagination (in production, use repository-level pagination)
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), items.size());

        if (start >= items.size()) {
            return List.of();
        }

        return items.subList(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

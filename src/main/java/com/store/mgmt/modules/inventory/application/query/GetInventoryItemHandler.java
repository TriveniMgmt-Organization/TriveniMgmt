package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.shared.domain.exception.ResourceNotFoundException;
import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Handler for GetInventoryItemQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetInventoryItemHandler implements QueryHandler<GetInventoryItemQuery, InventoryItemDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetInventoryItemHandler.class);

    private final InventoryItemRepository inventoryRepo;

    public GetInventoryItemHandler(InventoryItemRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    @Override
    public InventoryItemDTO handle(GetInventoryItemQuery query) {
        log.debug("Getting inventory item: {}", query.itemId());

        TenantContext tenant = TenantContext.current();
        tenant.requireStore(query.storeId());

        InventoryItem item = inventoryRepo.findByIdAndStoreId(query.itemId(), query.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem not found: " + query.itemId()));

        return toDTO(item);
    }

    private InventoryItemDTO toDTO(InventoryItem item) {
        StockLevel stockLevel = item.getStockLevel();
        boolean isLowStock = stockLevel != null && stockLevel.getAvailable() <= stockLevel.getLowStockThreshold();
        boolean isExpiringSoon = item.getExpiryDate() != null &&
                item.getExpiryDate().isBefore(LocalDate.now().plusDays(30));

        // Get variant info
        String variantSku = null;
        String variantName = null;
        if (item.getVariant() != null) {
            variantSku = item.getVariant().getSku();
            if (item.getVariant().getTemplate() != null) {
                variantName = item.getVariant().getTemplate().getName();
            }
        }

        // Get location name
        String locationName = item.getLocation() != null ? item.getLocation().getName() : null;

        return InventoryItemDTO.builder()
                .id(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .locationId(item.getLocation() != null ? item.getLocation().getId() : null)
                .storeId(item.getLocation() != null ? item.getLocation().getStoreId() : null)
                .variantSku(variantSku)
                .variantName(variantName)
                .locationName(locationName)
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

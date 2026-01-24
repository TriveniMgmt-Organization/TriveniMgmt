package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public GetInventoryItemsHandler(InventoryItemRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    @Override
    public List<InventoryItemDTO> handle(GetInventoryItemsQuery query) {
        log.debug("Getting inventory items for store: {}", query.storeId());

        TenantContext tenant = TenantContext.current();
        tenant.requireStore(query.storeId());

        List<InventoryItem> items;

        if (query.lowStockOnly()) {
            items = inventoryRepo.findLowStockByStoreId(StoreId.of(query.storeId()));
        } else if (query.expiringSoonOnly()) {
            items = inventoryRepo.findExpiringSoon(StoreId.of(query.storeId()), 30);
        } else if (query.locationId() != null) {
            items = inventoryRepo.findByLocationId(LocationId.of(query.locationId()));
        } else {
            items = inventoryRepo.findByStoreId(StoreId.of(query.storeId()));
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
        return InventoryItemDTO.builder()
                .id(item.getId().getValue())
                .variantId(item.getVariantId().getValue())
                .locationId(item.getLocationId().getValue())
                .storeId(item.getStoreId().getValue())
                .onHand(item.getStockLevel().onHand())
                .reserved(item.getStockLevel().reserved())
                .available(item.getStockLevel().available())
                .reorderPoint(item.getStockLevel().reorderPoint())
                .isLowStock(item.isLowStock())
                .batchNumber(item.getBatchNumber())
                .expiryDate(item.getExpiryDate())
                .isExpiringSoon(item.isExpiringSoon(30))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}

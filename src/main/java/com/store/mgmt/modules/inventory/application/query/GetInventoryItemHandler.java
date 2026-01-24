package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.domain.exception.EntityNotFoundException;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

        InventoryItem item = inventoryRepo.findByIdAndStoreId(
                InventoryItemId.of(query.itemId()),
                StoreId.of(query.storeId())
        ).orElseThrow(() -> new EntityNotFoundException("InventoryItem", query.itemId()));

        return toDTO(item);
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

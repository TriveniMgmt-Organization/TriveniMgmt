package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.domain.exception.EntityNotFoundException;
import com.store.mgmt.shared.infrastructure.event.DomainEventPublisher;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for ReceiveStockCommand.
 */
@Component
@Transactional
public class ReceiveStockHandler implements CommandHandler<ReceiveStockCommand, InventoryItemDTO> {

    private static final Logger log = LoggerFactory.getLogger(ReceiveStockHandler.class);

    private final InventoryItemRepository inventoryRepo;
    private final DomainEventPublisher eventPublisher;

    public ReceiveStockHandler(
            InventoryItemRepository inventoryRepo,
            DomainEventPublisher eventPublisher
    ) {
        this.inventoryRepo = inventoryRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public InventoryItemDTO handle(ReceiveStockCommand cmd) {
        log.debug("Receiving stock: item={}, quantity={}", cmd.itemId(), cmd.quantity());

        TenantContext tenant = TenantContext.current();
        tenant.requireStore(cmd.storeId());

        InventoryItem item = inventoryRepo.findByIdAndStoreId(
                InventoryItemId.of(cmd.itemId()),
                StoreId.of(cmd.storeId())
        ).orElseThrow(() -> new EntityNotFoundException("InventoryItem", cmd.itemId()));

        item.receiveStock(cmd.quantity(), cmd.reason(), UserId.of(tenant.userId()));

        InventoryItem saved = inventoryRepo.save(item);

        eventPublisher.publishAll(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Received {} units for item {}", cmd.quantity(), cmd.itemId());

        return toDTO(saved);
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

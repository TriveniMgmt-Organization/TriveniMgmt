package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.InventoryItemDTO;
import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.domain.exception.DuplicateEntityException;
import com.store.mgmt.shared.domain.exception.EntityNotFoundException;
import com.store.mgmt.shared.infrastructure.event.DomainEventPublisher;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateInventoryItemCommand.
 */
@Component
@Transactional
public class CreateInventoryItemHandler implements CommandHandler<CreateInventoryItemCommand, InventoryItemDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateInventoryItemHandler.class);

    private final InventoryItemRepository inventoryRepo;
    private final DomainEventPublisher eventPublisher;
    // These would be injected from other modules or as ports
    // For now, we'll validate IDs exist via the repository layer

    public CreateInventoryItemHandler(
            InventoryItemRepository inventoryRepo,
            DomainEventPublisher eventPublisher
    ) {
        this.inventoryRepo = inventoryRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public InventoryItemDTO handle(CreateInventoryItemCommand cmd) {
        log.debug("Creating inventory item: variant={}, location={}", cmd.variantId(), cmd.locationId());

        // Validate tenant context
        TenantContext tenant = TenantContext.current();
        tenant.requireStore(cmd.storeId());

        // Check for duplicate
        ProductVariantId variantId = ProductVariantId.of(cmd.variantId());
        LocationId locationId = LocationId.of(cmd.locationId());

        if (inventoryRepo.existsByVariantIdAndLocationId(variantId, locationId)) {
            throw new DuplicateEntityException(
                    "InventoryItem",
                    String.format("variant=%s, location=%s", cmd.variantId(), cmd.locationId())
            );
        }

        // Create domain object
        InventoryItem item = InventoryItem.create(
                variantId,
                locationId,
                StoreId.of(cmd.storeId()),
                cmd.initialQuantity(),
                cmd.lowStockThreshold(),
                UserId.of(tenant.userId())
        );

        // Set optional fields
        if (cmd.customBatchNumber() != null || cmd.expiryDate() != null) {
            item.updateBatchInfo(cmd.customBatchNumber(), cmd.expiryDate());
        }

        // Save
        InventoryItem saved = inventoryRepo.save(item);

        // Publish domain events
        eventPublisher.publishAll(saved.getDomainEvents());
        saved.clearDomainEvents();

        log.info("Created inventory item: {}", saved.getId().getValue());

        // Return DTO (in real impl, would include variant/location names from read model)
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

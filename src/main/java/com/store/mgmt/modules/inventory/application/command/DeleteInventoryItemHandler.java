package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.shared.domain.exception.ResourceNotFoundException;
import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteInventoryItemCommand.
 */
@Component
@Transactional
public class DeleteInventoryItemHandler implements CommandHandler<DeleteInventoryItemCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteInventoryItemHandler.class);

    private final InventoryItemRepository inventoryRepo;

    public DeleteInventoryItemHandler(InventoryItemRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    @Override
    public Void handle(DeleteInventoryItemCommand cmd) {
        log.debug("Deleting inventory item: {}", cmd.itemId());

        TenantContext tenant = TenantContext.current();
        tenant.requireStore(cmd.storeId());

        InventoryItem item = inventoryRepo.findByIdAndStoreId(cmd.itemId(), cmd.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem not found: " + cmd.itemId()));

        // Soft delete
        item.setDeletedAt(LocalDateTime.now());
        inventoryRepo.save(item);

        log.info("Deleted inventory item: {}", cmd.itemId());

        return null;
    }
}

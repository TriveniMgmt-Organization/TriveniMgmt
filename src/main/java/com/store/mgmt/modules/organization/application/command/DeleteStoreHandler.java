package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.domain.exception.StoreNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteStoreCommand.
 */
@Component
@Transactional
public class DeleteStoreHandler implements CommandHandler<DeleteStoreCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteStoreHandler.class);

    private final StoreRepository storeRepo;

    public DeleteStoreHandler(StoreRepository storeRepo) {
        this.storeRepo = storeRepo;
    }

    @Override
    public Void handle(DeleteStoreCommand cmd) {
        log.debug("Deleting store: {}", cmd.storeId());

        Store store = storeRepo.findById(cmd.storeId())
                .orElseThrow(() -> new StoreNotFoundException(cmd.storeId()));

        // Soft delete
        store.setDeletedAt(LocalDateTime.now());
        storeRepo.save(store);

        log.info("Deleted store: {}", cmd.storeId());

        return null;
    }
}

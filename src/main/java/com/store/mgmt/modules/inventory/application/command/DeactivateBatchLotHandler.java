package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.BatchLot;
import com.store.mgmt.modules.inventory.domain.repository.BatchLotRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for DeactivateBatchLotCommand.
 */
@Component
@Transactional
public class DeactivateBatchLotHandler implements CommandHandler<DeactivateBatchLotCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeactivateBatchLotHandler.class);

    private final BatchLotRepository batchLotRepository;

    public DeactivateBatchLotHandler(BatchLotRepository batchLotRepository) {
        this.batchLotRepository = batchLotRepository;
    }

    @Override
    public Void handle(DeactivateBatchLotCommand cmd) {
        log.debug("Deactivating batch/lot: {}", cmd.id());

        BatchLot batchLot = batchLotRepository.findById(cmd.id())
                .orElseThrow(() -> new EntityNotFoundException("Batch/lot not found with ID: " + cmd.id()));

        batchLot.setActive(false);
        batchLotRepository.save(batchLot);

        log.info("Deactivated batch/lot with ID: {}", cmd.id());
        return null;
    }
}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure;
import com.store.mgmt.modules.inventory.domain.repository.UnitOfMeasureRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteUnitOfMeasureCommand.
 */
@Component
@Transactional
public class DeleteUnitOfMeasureHandler implements CommandHandler<DeleteUnitOfMeasureCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteUnitOfMeasureHandler.class);

    private final UnitOfMeasureRepository uomRepository;

    public DeleteUnitOfMeasureHandler(UnitOfMeasureRepository uomRepository) {
        this.uomRepository = uomRepository;
    }

    @Override
    public Void handle(DeleteUnitOfMeasureCommand cmd) {
        log.debug("Deleting unit of measure: {}", cmd.id());

        UnitOfMeasure uom = uomRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Unit of measure not found with ID: " + cmd.id()));

        // Soft delete
        uom.setDeletedAt(LocalDateTime.now());
        uomRepository.save(uom);

        log.info("Soft deleted unit of measure with ID: {}", cmd.id());
        return null;
    }
}

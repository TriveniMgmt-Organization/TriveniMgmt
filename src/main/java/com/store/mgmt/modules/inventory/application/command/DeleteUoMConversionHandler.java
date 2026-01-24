package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.UoMConversion;
import com.store.mgmt.modules.inventory.domain.repository.UoMConversionRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteUoMConversionCommand.
 */
@Component
@Transactional
public class DeleteUoMConversionHandler implements CommandHandler<DeleteUoMConversionCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteUoMConversionHandler.class);

    private final UoMConversionRepository conversionRepository;

    public DeleteUoMConversionHandler(UoMConversionRepository conversionRepository) {
        this.conversionRepository = conversionRepository;
    }

    @Override
    public Void handle(DeleteUoMConversionCommand cmd) {
        log.debug("Deleting UoM conversion: {}", cmd.id());

        UoMConversion conversion = conversionRepository.findById(cmd.id())
                .orElseThrow(() -> new EntityNotFoundException("UoM conversion not found with ID: " + cmd.id()));

        // Soft delete
        conversion.setDeletedAt(LocalDateTime.now());
        conversionRepository.save(conversion);

        log.info("Soft deleted UoM conversion with ID: {}", cmd.id());
        return null;
    }
}

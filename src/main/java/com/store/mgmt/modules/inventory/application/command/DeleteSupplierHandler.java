package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Supplier;
import com.store.mgmt.modules.inventory.domain.repository.SupplierRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteSupplierCommand.
 */
@Component
@Transactional
public class DeleteSupplierHandler implements CommandHandler<DeleteSupplierCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteSupplierHandler.class);

    private final SupplierRepository supplierRepository;

    public DeleteSupplierHandler(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public Void handle(DeleteSupplierCommand cmd) {
        log.debug("Deleting supplier: {}", cmd.id());

        Supplier supplier = supplierRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + cmd.id()));

        // Soft delete
        supplier.setDeletedAt(LocalDateTime.now());
        supplierRepository.save(supplier);

        log.info("Soft deleted supplier with ID: {}", cmd.id());
        return null;
    }
}

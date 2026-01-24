package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Supplier;
import com.store.mgmt.modules.inventory.domain.repository.SupplierRepository;
import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SupplierMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateSupplierCommand.
 */
@Component
@Transactional
public class UpdateSupplierHandler implements CommandHandler<UpdateSupplierCommand, SupplierResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateSupplierHandler.class);

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public UpdateSupplierHandler(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public SupplierResponseDTO handle(UpdateSupplierCommand cmd) {
        log.debug("Updating supplier: {}", cmd.id());

        Supplier supplier = supplierRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + cmd.id()));

        // Check for duplicate name if name is being changed
        if (cmd.name() != null && !cmd.name().equals(supplier.getName())) {
            supplierRepository.findByNameAndOrganizationId(cmd.name(), cmd.organizationId()).ifPresent(existing -> {
                if (!existing.getId().equals(cmd.id())) {
                    throw new IllegalArgumentException("Supplier with name '" + cmd.name() + "' already exists in this organization");
                }
            });
            supplier.setName(cmd.name());
        }

        if (cmd.contactPerson() != null) {
            supplier.setContactPerson(cmd.contactPerson());
        }
        if (cmd.email() != null) {
            supplier.setEmail(cmd.email());
        }
        if (cmd.phone() != null) {
            supplier.setPhone(cmd.phone());
        }
        if (cmd.address() != null) {
            supplier.setAddress(cmd.address());
        }
        if (cmd.accountNumber() != null) {
            supplier.setAccountNumber(cmd.accountNumber());
        }

        Supplier saved = supplierRepository.save(supplier);
        log.info("Updated supplier with ID: {}", saved.getId());

        return supplierMapper.toResponseDTO(saved);
    }
}

package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Supplier;
import com.store.mgmt.modules.inventory.domain.repository.SupplierRepository;
import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SupplierMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateSupplierCommand.
 */
@Component
@Transactional
public class CreateSupplierHandler implements CommandHandler<CreateSupplierCommand, SupplierResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateSupplierHandler.class);

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public CreateSupplierHandler(
            SupplierRepository supplierRepository,
            SupplierMapper supplierMapper
    ) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public SupplierResponseDTO handle(CreateSupplierCommand cmd) {
        log.debug("Creating supplier: {} for organization: {}", cmd.name(), cmd.organizationId());

        // Check for duplicate name within organization
        supplierRepository.findByNameAndOrganizationId(cmd.name(), cmd.organizationId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Supplier with name '" + cmd.name() + "' already exists in this organization");
        });

        Supplier supplier = new Supplier();
        supplier.setOrganizationId(cmd.organizationId());
        supplier.setName(cmd.name());
        supplier.setContactPerson(cmd.contactPerson());
        supplier.setEmail(cmd.email());
        supplier.setPhone(cmd.phone());
        supplier.setAddress(cmd.address());
        supplier.setAccountNumber(cmd.accountNumber());

        Supplier saved = supplierRepository.save(supplier);
        log.info("Created supplier with ID: {}", saved.getId());

        return supplierMapper.toResponseDTO(saved);
    }
}

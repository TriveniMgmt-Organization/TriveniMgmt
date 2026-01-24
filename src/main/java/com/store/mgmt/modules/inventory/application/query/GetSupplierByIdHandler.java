package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Supplier;
import com.store.mgmt.modules.inventory.domain.repository.SupplierRepository;
import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SupplierMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetSupplierByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetSupplierByIdHandler implements QueryHandler<GetSupplierByIdQuery, SupplierResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetSupplierByIdHandler.class);

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public GetSupplierByIdHandler(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public SupplierResponseDTO handle(GetSupplierByIdQuery query) {
        log.debug("Getting supplier by ID: {}", query.id());

        Supplier supplier = supplierRepository.findByIdAndOrganizationId(query.id(), query.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + query.id()));

        return supplierMapper.toResponseDTO(supplier);
    }
}

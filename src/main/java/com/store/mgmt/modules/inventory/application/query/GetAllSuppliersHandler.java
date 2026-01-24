package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Supplier;
import com.store.mgmt.modules.inventory.domain.repository.SupplierRepository;
import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SupplierMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetAllSuppliersQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllSuppliersHandler implements QueryHandler<GetAllSuppliersQuery, List<SupplierResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllSuppliersHandler.class);

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public GetAllSuppliersHandler(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public List<SupplierResponseDTO> handle(GetAllSuppliersQuery query) {
        log.debug("Getting all suppliers for organization: {}", query.organizationId());

        List<Supplier> suppliers = supplierRepository.findByOrganizationId(query.organizationId())
                .stream()
                .filter(s -> s.getDeletedAt() == null)
                .toList();

        return supplierMapper.toResponseDTOList(suppliers);
    }
}

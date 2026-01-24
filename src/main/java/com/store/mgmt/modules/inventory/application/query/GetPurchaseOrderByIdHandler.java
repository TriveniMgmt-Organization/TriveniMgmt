package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.PurchaseOrder;
import com.store.mgmt.modules.inventory.domain.repository.PurchaseOrderRepository;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.modules.inventory.application.service.PurchaseOrderMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetPurchaseOrderByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetPurchaseOrderByIdHandler implements QueryHandler<GetPurchaseOrderByIdQuery, PurchaseOrderResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetPurchaseOrderByIdHandler.class);

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderMapper mapper;

    public GetPurchaseOrderByIdHandler(PurchaseOrderRepository poRepository, PurchaseOrderMapper mapper) {
        this.poRepository = poRepository;
        this.mapper = mapper;
    }

    @Override
    public PurchaseOrderResponseDTO handle(GetPurchaseOrderByIdQuery query) {
        log.debug("Getting purchase order by ID: {}", query.id());

        PurchaseOrder po = poRepository.findByIdAndOrganizationId(query.id(), query.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with ID: " + query.id()));

        return mapper.toResponseDTO(po);
    }
}

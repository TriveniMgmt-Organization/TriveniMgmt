package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.PurchaseOrder;
import com.store.mgmt.modules.inventory.domain.repository.PurchaseOrderRepository;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.modules.inventory.application.service.PurchaseOrderMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetAllPurchaseOrdersQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllPurchaseOrdersHandler implements QueryHandler<GetAllPurchaseOrdersQuery, List<PurchaseOrderResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllPurchaseOrdersHandler.class);

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderMapper mapper;

    public GetAllPurchaseOrdersHandler(PurchaseOrderRepository poRepository, PurchaseOrderMapper mapper) {
        this.poRepository = poRepository;
        this.mapper = mapper;
    }

    @Override
    public List<PurchaseOrderResponseDTO> handle(GetAllPurchaseOrdersQuery query) {
        log.debug("Getting all purchase orders for organization: {}", query.organizationId());

        List<PurchaseOrder> orders = poRepository.findByOrganizationId(query.organizationId());

        return mapper.toResponseDTOList(orders);
    }
}

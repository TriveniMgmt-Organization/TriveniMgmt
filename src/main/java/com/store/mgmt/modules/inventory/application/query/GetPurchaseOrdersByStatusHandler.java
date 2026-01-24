package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.PurchaseOrder;
import com.store.mgmt.modules.inventory.domain.model.PurchaseOrderStatus;
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
 * Handler for GetPurchaseOrdersByStatusQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetPurchaseOrdersByStatusHandler implements QueryHandler<GetPurchaseOrdersByStatusQuery, List<PurchaseOrderResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetPurchaseOrdersByStatusHandler.class);

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderMapper mapper;

    public GetPurchaseOrdersByStatusHandler(PurchaseOrderRepository poRepository, PurchaseOrderMapper mapper) {
        this.poRepository = poRepository;
        this.mapper = mapper;
    }

    @Override
    public List<PurchaseOrderResponseDTO> handle(GetPurchaseOrdersByStatusQuery query) {
        log.debug("Getting purchase orders by status: {} for organization: {}",
                query.status(), query.organizationId());

        PurchaseOrderStatus status;
        try {
            status = PurchaseOrderStatus.valueOf(query.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid purchase order status: " + query.status());
        }

        List<PurchaseOrder> orders = poRepository.findByStatusAndOrganizationId(status, query.organizationId());

        return mapper.toResponseDTOList(orders);
    }
}

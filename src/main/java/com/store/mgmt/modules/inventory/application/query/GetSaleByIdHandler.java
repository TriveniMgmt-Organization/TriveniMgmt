package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Sale;
import com.store.mgmt.modules.inventory.domain.repository.SaleRepository;
import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SaleMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetSaleByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetSaleByIdHandler implements QueryHandler<GetSaleByIdQuery, SaleResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetSaleByIdHandler.class);

    private final SaleRepository saleRepository;
    private final SaleMapper mapper;

    public GetSaleByIdHandler(SaleRepository saleRepository, SaleMapper mapper) {
        this.saleRepository = saleRepository;
        this.mapper = mapper;
    }

    @Override
    public SaleResponseDTO handle(GetSaleByIdQuery query) {
        log.debug("Getting sale by ID: {}", query.id());

        Sale sale = saleRepository.findByIdAndStoreId(query.id(), query.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Sale not found: " + query.id()));

        return mapper.toResponseDTO(sale);
    }
}

package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Sale;
import com.store.mgmt.modules.inventory.domain.repository.SaleRepository;
import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SaleMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetSalesByDateRangeQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetSalesByDateRangeHandler implements QueryHandler<GetSalesByDateRangeQuery, List<SaleResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetSalesByDateRangeHandler.class);

    private final SaleRepository saleRepository;
    private final SaleMapper mapper;

    public GetSalesByDateRangeHandler(SaleRepository saleRepository, SaleMapper mapper) {
        this.saleRepository = saleRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SaleResponseDTO> handle(GetSalesByDateRangeQuery query) {
        log.debug("Getting sales for store {} between {} and {}",
                query.storeId(), query.startDate(), query.endDate());

        List<Sale> sales = saleRepository.findBySaleTimestampBetweenAndStoreId(
                query.startDate(),
                query.endDate(),
                query.storeId()
        );

        return mapper.toResponseDTOList(sales);
    }
}

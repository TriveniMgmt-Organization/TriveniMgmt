package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.application.dto.StockLevelResponseDTO;
import com.store.mgmt.modules.inventory.application.service.StockLevelMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetStockLevelsByVariantQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetStockLevelsByVariantHandler implements QueryHandler<GetStockLevelsByVariantQuery, List<StockLevelResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetStockLevelsByVariantHandler.class);

    private final StockLevelRepository stockLevelRepository;
    private final StockLevelMapper mapper;

    public GetStockLevelsByVariantHandler(StockLevelRepository stockLevelRepository, StockLevelMapper mapper) {
        this.stockLevelRepository = stockLevelRepository;
        this.mapper = mapper;
    }

    @Override
    public List<StockLevelResponseDTO> handle(GetStockLevelsByVariantQuery query) {
        log.debug("Getting stock levels for variant: {}", query.variantId());

        List<StockLevel> stockLevels = stockLevelRepository.findByVariantId(query.variantId());

        return mapper.toResponseDTOList(stockLevels);
    }
}

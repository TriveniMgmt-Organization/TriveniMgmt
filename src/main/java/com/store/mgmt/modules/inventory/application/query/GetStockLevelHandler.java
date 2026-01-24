package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.application.dto.StockLevelResponseDTO;
import com.store.mgmt.modules.inventory.application.service.StockLevelMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetStockLevelQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetStockLevelHandler implements QueryHandler<GetStockLevelQuery, StockLevelResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetStockLevelHandler.class);

    private final StockLevelRepository stockLevelRepository;
    private final StockLevelMapper mapper;

    public GetStockLevelHandler(StockLevelRepository stockLevelRepository, StockLevelMapper mapper) {
        this.stockLevelRepository = stockLevelRepository;
        this.mapper = mapper;
    }

    @Override
    public StockLevelResponseDTO handle(GetStockLevelQuery query) {
        log.debug("Getting stock level for inventory item: {}", query.inventoryItemId());

        StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(query.inventoryItemId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Stock level not found for inventory item: " + query.inventoryItemId()
                ));

        return mapper.toResponseDTO(stockLevel);
    }
}

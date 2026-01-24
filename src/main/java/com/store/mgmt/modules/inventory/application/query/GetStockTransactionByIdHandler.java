package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.StockTransaction;
import com.store.mgmt.modules.inventory.domain.repository.StockTransactionRepository;
import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import com.store.mgmt.modules.inventory.application.service.StockTransactionMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetStockTransactionByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetStockTransactionByIdHandler implements QueryHandler<GetStockTransactionByIdQuery, StockTransactionResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetStockTransactionByIdHandler.class);

    private final StockTransactionRepository transactionRepository;
    private final StockTransactionMapper mapper;

    public GetStockTransactionByIdHandler(StockTransactionRepository transactionRepository, StockTransactionMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @Override
    public StockTransactionResponseDTO handle(GetStockTransactionByIdQuery query) {
        log.debug("Getting stock transaction by ID: {}", query.id());

        StockTransaction transaction = transactionRepository.findById(query.id())
                .orElseThrow(() -> new EntityNotFoundException("Stock transaction not found with ID: " + query.id()));

        return mapper.toResponseDTO(transaction);
    }
}

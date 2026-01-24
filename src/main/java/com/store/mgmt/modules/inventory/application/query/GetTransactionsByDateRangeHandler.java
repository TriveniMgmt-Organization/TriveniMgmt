package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.StockTransaction;
import com.store.mgmt.modules.inventory.domain.repository.StockTransactionRepository;
import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import com.store.mgmt.modules.inventory.application.service.StockTransactionMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetTransactionsByDateRangeQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetTransactionsByDateRangeHandler implements QueryHandler<GetTransactionsByDateRangeQuery, List<StockTransactionResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetTransactionsByDateRangeHandler.class);

    private final StockTransactionRepository transactionRepository;
    private final StockTransactionMapper mapper;

    public GetTransactionsByDateRangeHandler(StockTransactionRepository transactionRepository, StockTransactionMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @Override
    public List<StockTransactionResponseDTO> handle(GetTransactionsByDateRangeQuery query) {
        log.debug("Getting transactions between {} and {}", query.startDate(), query.endDate());

        List<StockTransaction> transactions = transactionRepository.findByTimestampBetween(
                query.startDate(), query.endDate()
        );

        return mapper.toResponseDTOList(transactions);
    }
}

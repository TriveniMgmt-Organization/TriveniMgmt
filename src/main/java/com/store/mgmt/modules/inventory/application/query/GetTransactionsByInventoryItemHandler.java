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
 * Handler for GetTransactionsByInventoryItemQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetTransactionsByInventoryItemHandler implements QueryHandler<GetTransactionsByInventoryItemQuery, List<StockTransactionResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetTransactionsByInventoryItemHandler.class);

    private final StockTransactionRepository transactionRepository;
    private final StockTransactionMapper mapper;

    public GetTransactionsByInventoryItemHandler(StockTransactionRepository transactionRepository, StockTransactionMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @Override
    public List<StockTransactionResponseDTO> handle(GetTransactionsByInventoryItemQuery query) {
        log.debug("Getting transactions for inventory item: {}", query.inventoryItemId());

        List<StockTransaction> transactions = transactionRepository.findByInventoryItemIdOrderByTimestampDesc(query.inventoryItemId());

        return mapper.toResponseDTOList(transactions);
    }
}

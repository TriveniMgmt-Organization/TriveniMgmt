package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.BatchLot;
import com.store.mgmt.modules.inventory.domain.repository.BatchLotRepository;
import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import com.store.mgmt.modules.inventory.application.service.BatchLotMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Handler for GetExpiringBatchLotsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetExpiringBatchLotsHandler implements QueryHandler<GetExpiringBatchLotsQuery, List<BatchLotResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetExpiringBatchLotsHandler.class);

    private final BatchLotRepository batchLotRepository;
    private final BatchLotMapper mapper;

    public GetExpiringBatchLotsHandler(BatchLotRepository batchLotRepository, BatchLotMapper mapper) {
        this.batchLotRepository = batchLotRepository;
        this.mapper = mapper;
    }

    @Override
    public List<BatchLotResponseDTO> handle(GetExpiringBatchLotsQuery query) {
        log.debug("Getting batch/lots expiring within {} days", query.daysAhead());

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(query.daysAhead());

        List<BatchLot> expiringBatchLots = batchLotRepository.findExpiringBetween(startDate, endDate);

        return mapper.toResponseDTOList(expiringBatchLots);
    }
}

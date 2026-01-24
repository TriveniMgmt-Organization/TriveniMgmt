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

import java.util.List;

/**
 * Handler for GetAllBatchLotsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllBatchLotsHandler implements QueryHandler<GetAllBatchLotsQuery, List<BatchLotResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllBatchLotsHandler.class);

    private final BatchLotRepository batchLotRepository;
    private final BatchLotMapper mapper;

    public GetAllBatchLotsHandler(BatchLotRepository batchLotRepository, BatchLotMapper mapper) {
        this.batchLotRepository = batchLotRepository;
        this.mapper = mapper;
    }

    @Override
    public List<BatchLotResponseDTO> handle(GetAllBatchLotsQuery query) {
        log.debug("Getting all batch/lots, includeInactive: {}", query.includeInactive());

        List<BatchLot> batchLots = batchLotRepository.findAll().stream()
                .filter(bl -> bl.getDeletedAt() == null)
                .filter(bl -> query.includeInactive() || bl.isActive())
                .toList();

        return mapper.toResponseDTOList(batchLots);
    }
}

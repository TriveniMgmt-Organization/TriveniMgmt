package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.BatchLot;
import com.store.mgmt.modules.inventory.domain.repository.BatchLotRepository;
import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import com.store.mgmt.modules.inventory.application.service.BatchLotMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetBatchLotByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetBatchLotByIdHandler implements QueryHandler<GetBatchLotByIdQuery, BatchLotResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetBatchLotByIdHandler.class);

    private final BatchLotRepository batchLotRepository;
    private final BatchLotMapper mapper;

    public GetBatchLotByIdHandler(BatchLotRepository batchLotRepository, BatchLotMapper mapper) {
        this.batchLotRepository = batchLotRepository;
        this.mapper = mapper;
    }

    @Override
    public BatchLotResponseDTO handle(GetBatchLotByIdQuery query) {
        log.debug("Getting batch/lot by ID: {}", query.id());

        BatchLot batchLot = batchLotRepository.findById(query.id())
                .filter(bl -> bl.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Batch/lot not found with ID: " + query.id()));

        return mapper.toResponseDTO(batchLot);
    }
}

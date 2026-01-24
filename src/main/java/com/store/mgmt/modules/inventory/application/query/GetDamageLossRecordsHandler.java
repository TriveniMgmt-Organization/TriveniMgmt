package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.DamageLoss;
import com.store.mgmt.modules.inventory.domain.repository.DamageLossRepository;
import com.store.mgmt.modules.inventory.application.dto.DamageLossResponseDTO;
import com.store.mgmt.modules.inventory.application.service.DamageLossMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handler for GetDamageLossRecordsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetDamageLossRecordsHandler implements QueryHandler<GetDamageLossRecordsQuery, List<DamageLossResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetDamageLossRecordsHandler.class);

    private final DamageLossRepository damageLossRepository;
    private final DamageLossMapper mapper;

    public GetDamageLossRecordsHandler(DamageLossRepository damageLossRepository, DamageLossMapper mapper) {
        this.damageLossRepository = damageLossRepository;
        this.mapper = mapper;
    }

    @Override
    public List<DamageLossResponseDTO> handle(GetDamageLossRecordsQuery query) {
        log.debug("Getting damage/loss records, storeId: {}, locationId: {}, startDate: {}, endDate: {}",
                query.storeId(), query.locationId(), query.startDate(), query.endDate());

        List<DamageLoss> records;

        if (query.locationId() != null && query.storeId() != null) {
            records = damageLossRepository.findByLocationIdAndStoreId(query.locationId(), query.storeId());
        } else if (query.startDate() != null && query.endDate() != null && query.storeId() != null) {
            LocalDateTime start = query.startDate().atStartOfDay();
            LocalDateTime end = query.endDate().plusDays(1).atStartOfDay();
            records = damageLossRepository.findByDateRecordedBetweenAndStoreId(start, end, query.storeId());
        } else if (query.startDate() != null && query.endDate() != null) {
            LocalDateTime start = query.startDate().atStartOfDay();
            LocalDateTime end = query.endDate().plusDays(1).atStartOfDay();
            records = damageLossRepository.findByDateRecordedBetween(start, end);
        } else if (query.locationId() != null) {
            records = damageLossRepository.findByLocationId(query.locationId());
        } else {
            records = damageLossRepository.findAll();
        }

        return mapper.toResponseDTOList(records);
    }
}

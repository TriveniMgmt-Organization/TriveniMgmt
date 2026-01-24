package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.DamageLoss;
import com.store.mgmt.modules.inventory.domain.repository.DamageLossRepository;
import com.store.mgmt.modules.inventory.application.dto.DamageLossResponseDTO;
import com.store.mgmt.modules.inventory.application.service.DamageLossMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetDamageLossByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetDamageLossByIdHandler implements QueryHandler<GetDamageLossByIdQuery, DamageLossResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetDamageLossByIdHandler.class);

    private final DamageLossRepository damageLossRepository;
    private final DamageLossMapper mapper;

    public GetDamageLossByIdHandler(DamageLossRepository damageLossRepository, DamageLossMapper mapper) {
        this.damageLossRepository = damageLossRepository;
        this.mapper = mapper;
    }

    @Override
    public DamageLossResponseDTO handle(GetDamageLossByIdQuery query) {
        log.debug("Getting damage/loss by ID: {}", query.id());

        DamageLoss damageLoss = damageLossRepository.findByIdAndOrganizationId(query.id(), query.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Damage/loss record not found with ID: " + query.id()));

        return mapper.toResponseDTO(damageLoss);
    }
}

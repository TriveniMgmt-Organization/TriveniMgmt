package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure;
import com.store.mgmt.modules.inventory.domain.repository.UnitOfMeasureRepository;
import com.store.mgmt.modules.inventory.application.dto.UnitOfMeasureResponseDTO;
import com.store.mgmt.modules.inventory.application.service.UnitOfMeasureMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetUnitOfMeasureByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetUnitOfMeasureByIdHandler implements QueryHandler<GetUnitOfMeasureByIdQuery, UnitOfMeasureResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetUnitOfMeasureByIdHandler.class);

    private final UnitOfMeasureRepository uomRepository;
    private final UnitOfMeasureMapper uomMapper;

    public GetUnitOfMeasureByIdHandler(UnitOfMeasureRepository uomRepository, UnitOfMeasureMapper uomMapper) {
        this.uomRepository = uomRepository;
        this.uomMapper = uomMapper;
    }

    @Override
    public UnitOfMeasureResponseDTO handle(GetUnitOfMeasureByIdQuery query) {
        log.debug("Getting unit of measure by ID: {}", query.id());

        UnitOfMeasure uom = uomRepository.findByIdAndOrganizationId(query.id(), query.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Unit of measure not found with ID: " + query.id()));

        return uomMapper.toResponseDTO(uom);
    }
}

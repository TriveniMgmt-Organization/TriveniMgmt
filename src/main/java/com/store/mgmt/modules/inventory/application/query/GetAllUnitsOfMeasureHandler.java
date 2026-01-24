package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure;
import com.store.mgmt.modules.inventory.domain.repository.UnitOfMeasureRepository;
import com.store.mgmt.modules.inventory.application.dto.UnitOfMeasureResponseDTO;
import com.store.mgmt.modules.inventory.application.service.UnitOfMeasureMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetAllUnitsOfMeasureQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllUnitsOfMeasureHandler implements QueryHandler<GetAllUnitsOfMeasureQuery, List<UnitOfMeasureResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllUnitsOfMeasureHandler.class);

    private final UnitOfMeasureRepository uomRepository;
    private final UnitOfMeasureMapper uomMapper;

    public GetAllUnitsOfMeasureHandler(UnitOfMeasureRepository uomRepository, UnitOfMeasureMapper uomMapper) {
        this.uomRepository = uomRepository;
        this.uomMapper = uomMapper;
    }

    @Override
    public List<UnitOfMeasureResponseDTO> handle(GetAllUnitsOfMeasureQuery query) {
        log.debug("Getting all units of measure for organization: {}", query.organizationId());

        List<UnitOfMeasure> uoms = uomRepository.findByOrganizationId(query.organizationId())
                .stream()
                .filter(uom -> uom.getDeletedAt() == null)
                .toList();

        return uomMapper.toResponseDTOList(uoms);
    }
}

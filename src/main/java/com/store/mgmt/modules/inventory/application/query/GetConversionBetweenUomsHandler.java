package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.UoMConversion;
import com.store.mgmt.modules.inventory.domain.repository.UoMConversionRepository;
import com.store.mgmt.modules.inventory.application.dto.UoMConversionResponseDTO;
import com.store.mgmt.modules.inventory.application.service.UoMConversionMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetConversionBetweenUomsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetConversionBetweenUomsHandler implements QueryHandler<GetConversionBetweenUomsQuery, UoMConversionResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetConversionBetweenUomsHandler.class);

    private final UoMConversionRepository conversionRepository;
    private final UoMConversionMapper mapper;

    public GetConversionBetweenUomsHandler(UoMConversionRepository conversionRepository, UoMConversionMapper mapper) {
        this.conversionRepository = conversionRepository;
        this.mapper = mapper;
    }

    @Override
    public UoMConversionResponseDTO handle(GetConversionBetweenUomsQuery query) {
        log.debug("Getting conversion from UoM {} to UoM {}", query.fromUomId(), query.toUomId());

        UoMConversion conversion = conversionRepository.findByFromUomIdAndToUomId(query.fromUomId(), query.toUomId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No conversion found from UoM " + query.fromUomId() + " to UoM " + query.toUomId()
                ));

        return mapper.toResponseDTO(conversion);
    }
}

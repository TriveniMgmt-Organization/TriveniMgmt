package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.UoMConversion;
import com.store.mgmt.modules.inventory.domain.repository.UoMConversionRepository;
import com.store.mgmt.modules.inventory.application.dto.UoMConversionResponseDTO;
import com.store.mgmt.modules.inventory.application.service.UoMConversionMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetAllUoMConversionsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllUoMConversionsHandler implements QueryHandler<GetAllUoMConversionsQuery, List<UoMConversionResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllUoMConversionsHandler.class);

    private final UoMConversionRepository conversionRepository;
    private final UoMConversionMapper mapper;

    public GetAllUoMConversionsHandler(UoMConversionRepository conversionRepository, UoMConversionMapper mapper) {
        this.conversionRepository = conversionRepository;
        this.mapper = mapper;
    }

    @Override
    public List<UoMConversionResponseDTO> handle(GetAllUoMConversionsQuery query) {
        log.debug("Getting all UoM conversions");

        List<UoMConversion> conversions = conversionRepository.findAll().stream()
                .filter(c -> c.getDeletedAt() == null)
                .toList();

        return mapper.toResponseDTOList(conversions);
    }
}

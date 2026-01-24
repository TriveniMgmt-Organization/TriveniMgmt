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
 * Handler for GetUoMConversionByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetUoMConversionByIdHandler implements QueryHandler<GetUoMConversionByIdQuery, UoMConversionResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetUoMConversionByIdHandler.class);

    private final UoMConversionRepository conversionRepository;
    private final UoMConversionMapper mapper;

    public GetUoMConversionByIdHandler(UoMConversionRepository conversionRepository, UoMConversionMapper mapper) {
        this.conversionRepository = conversionRepository;
        this.mapper = mapper;
    }

    @Override
    public UoMConversionResponseDTO handle(GetUoMConversionByIdQuery query) {
        log.debug("Getting UoM conversion by ID: {}", query.id());

        UoMConversion conversion = conversionRepository.findById(query.id())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("UoM conversion not found with ID: " + query.id()));

        return mapper.toResponseDTO(conversion);
    }
}

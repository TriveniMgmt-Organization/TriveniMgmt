package com.store.mgmt.modules.globaltemplates.application.query;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.modules.globaltemplates.application.service.GlobalTemplateMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class GetTemplateByCodeHandler implements QueryHandler<GetTemplateByCodeQuery, GlobalTemplateResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetTemplateByCodeHandler.class);

    private final GlobalTemplateRepository repository;
    private final GlobalTemplateMapper mapper;

    public GetTemplateByCodeHandler(GlobalTemplateRepository repository, GlobalTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GlobalTemplateResponseDTO handle(GetTemplateByCodeQuery query) {
        log.debug("Getting template by code: {}", query.code());

        GlobalTemplate template = repository.findByCode(query.code())
                .orElseThrow(() -> new EntityNotFoundException("Template not found with code: " + query.code()));

        return mapper.toResponseDTO(template);
    }
}

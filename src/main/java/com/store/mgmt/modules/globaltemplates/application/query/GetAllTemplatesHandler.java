package com.store.mgmt.modules.globaltemplates.application.query;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.modules.globaltemplates.application.service.GlobalTemplateMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class GetAllTemplatesHandler implements QueryHandler<GetAllTemplatesQuery, List<GlobalTemplateResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllTemplatesHandler.class);

    private final GlobalTemplateRepository repository;
    private final GlobalTemplateMapper mapper;

    public GetAllTemplatesHandler(GlobalTemplateRepository repository, GlobalTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<GlobalTemplateResponseDTO> handle(GetAllTemplatesQuery query) {
        log.debug("Getting all templates");

        List<GlobalTemplate> templates = repository.findAll();
        return mapper.toResponseDTOList(templates);
    }
}

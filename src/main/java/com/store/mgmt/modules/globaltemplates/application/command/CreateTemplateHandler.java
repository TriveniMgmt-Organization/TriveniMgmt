package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.modules.globaltemplates.application.service.GlobalTemplateMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CreateTemplateHandler implements CommandHandler<CreateTemplateCommand, GlobalTemplateResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateTemplateHandler.class);

    private final GlobalTemplateRepository repository;
    private final GlobalTemplateMapper mapper;

    public CreateTemplateHandler(GlobalTemplateRepository repository, GlobalTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GlobalTemplateResponseDTO handle(CreateTemplateCommand cmd) {
        log.info("Creating global template: {}", cmd.code());

        // Check for duplicate code
        if (repository.findByCode(cmd.code()).isPresent()) {
            throw new IllegalArgumentException("Template with code '" + cmd.code() + "' already exists");
        }

        GlobalTemplate template = new GlobalTemplate();
        template.setName(cmd.name());
        template.setCode(cmd.code().toUpperCase());
        template.setType(cmd.type().toUpperCase());
        template.setDescription(cmd.description());
        template.setIsActive(cmd.isActive() != null ? cmd.isActive() : true);

        GlobalTemplate saved = repository.save(template);
        log.info("Created global template with ID: {}", saved.getId());

        return mapper.toResponseDTO(saved);
    }
}

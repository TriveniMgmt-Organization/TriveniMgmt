package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import com.store.mgmt.modules.globaltemplates.application.dto.GlobalTemplateResponseDTO;
import com.store.mgmt.modules.globaltemplates.application.service.GlobalTemplateMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UpdateTemplateHandler implements CommandHandler<UpdateTemplateCommand, GlobalTemplateResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateTemplateHandler.class);

    private final GlobalTemplateRepository repository;
    private final GlobalTemplateMapper mapper;

    public UpdateTemplateHandler(GlobalTemplateRepository repository, GlobalTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GlobalTemplateResponseDTO handle(UpdateTemplateCommand cmd) {
        log.info("Updating global template: {}", cmd.templateId());

        GlobalTemplate template = repository.findByIdWithItems(cmd.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Template not found: " + cmd.templateId()));

        if (cmd.name() != null) {
            template.setName(cmd.name());
        }
        if (cmd.type() != null) {
            template.setType(cmd.type().toUpperCase());
        }
        if (cmd.description() != null) {
            template.setDescription(cmd.description());
        }
        if (cmd.isActive() != null) {
            template.setIsActive(cmd.isActive());
        }

        GlobalTemplate saved = repository.save(template);
        log.info("Updated global template: {}", saved.getId());

        return mapper.toResponseDTO(saved);
    }
}

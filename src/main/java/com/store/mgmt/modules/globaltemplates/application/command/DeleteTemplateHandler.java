package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplate;
import com.store.mgmt.modules.globaltemplates.domain.repository.GlobalTemplateRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Transactional
public class DeleteTemplateHandler implements CommandHandler<DeleteTemplateCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteTemplateHandler.class);

    private final GlobalTemplateRepository repository;

    public DeleteTemplateHandler(GlobalTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void handle(DeleteTemplateCommand cmd) {
        log.info("Deleting global template: {}", cmd.templateId());

        GlobalTemplate template = repository.findByIdWithItems(cmd.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Template not found: " + cmd.templateId()));

        // Soft delete
        template.setDeletedAt(LocalDateTime.now());
        repository.save(template);

        log.info("Deleted global template: {}", cmd.templateId());
        return null;
    }
}

package com.store.mgmt.modules.globaltemplates.application.command;

import com.store.mgmt.modules.globaltemplates.domain.service.TemplateCopyService;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("globalTemplatesApplyTemplateHandler")
@Transactional
public class ApplyTemplateHandler implements CommandHandler<ApplyTemplateCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(ApplyTemplateHandler.class);

    private final OrganizationRepository organizationRepository;
    private final TemplateCopyService templateCopyService;

    public ApplyTemplateHandler(
            OrganizationRepository organizationRepository,
            TemplateCopyService templateCopyService
    ) {
        this.organizationRepository = organizationRepository;
        this.templateCopyService = templateCopyService;
    }

    @Override
    public Void handle(ApplyTemplateCommand cmd) {
        log.info("Applying template '{}' to organization: {}", cmd.templateCode(), cmd.organizationId());

        Organization organization = organizationRepository.findById(cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + cmd.organizationId()));

        templateCopyService.applyTemplate(organization, cmd.templateCode());

        // Update organization with applied template code
        organization.setAppliedTemplateCode(cmd.templateCode());
        organizationRepository.save(organization);

        log.info("Template '{}' applied successfully to organization: {}", cmd.templateCode(), cmd.organizationId());
        return null;
    }
}

package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.globaltemplates.domain.service.TemplateCopyService;
import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.exception.TemplateAlreadyAppliedException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for ApplyTemplateCommand.
 * Applies a global template to an organization, copying all template items
 * (categories, brands, UoMs, etc.) to the organization.
 */
@Component
@Transactional
public class ApplyTemplateHandler implements CommandHandler<ApplyTemplateCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(ApplyTemplateHandler.class);

    private final OrganizationRepository orgRepo;
    private final TemplateCopyService templateCopyService;

    public ApplyTemplateHandler(
            OrganizationRepository orgRepo,
            TemplateCopyService templateCopyService
    ) {
        this.orgRepo = orgRepo;
        this.templateCopyService = templateCopyService;
    }

    @Override
    public Void handle(ApplyTemplateCommand cmd) {
        log.info("Applying template '{}' to organization: {}", cmd.templateCode(), cmd.organizationId());

        // Fetch organization with stores (needed for location copying)
        Organization org = orgRepo.findByIdWithStores(cmd.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(cmd.organizationId()));

        // Check if template already applied
        if (org.getAppliedTemplateCode() != null) {
            throw new TemplateAlreadyAppliedException(org.getId(), org.getAppliedTemplateCode());
        }

        // Copy template items (brands, categories, UoMs, etc.) to the organization
        templateCopyService.applyTemplate(org, cmd.templateCode());

        // Mark template as applied
        org.setAppliedTemplateCode(cmd.templateCode());
        orgRepo.save(org);

        log.info("Successfully applied template '{}' to organization: {}", cmd.templateCode(), cmd.organizationId());

        return null;
    }
}

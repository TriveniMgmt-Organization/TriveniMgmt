package com.store.mgmt.modules.organization.application.command;

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
 */
@Component
@Transactional
public class ApplyTemplateHandler implements CommandHandler<ApplyTemplateCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(ApplyTemplateHandler.class);

    private final OrganizationRepository orgRepo;

    public ApplyTemplateHandler(OrganizationRepository orgRepo) {
        this.orgRepo = orgRepo;
    }

    @Override
    public Void handle(ApplyTemplateCommand cmd) {
        log.debug("Applying template {} to organization: {}", cmd.templateCode(), cmd.organizationId());

        Organization org = orgRepo.findById(cmd.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(cmd.organizationId()));

        // Check if template already applied
        if (org.getAppliedTemplateCode() != null) {
            throw new TemplateAlreadyAppliedException(org.getId(), org.getAppliedTemplateCode());
        }

        org.setAppliedTemplateCode(cmd.templateCode());
        orgRepo.save(org);

        log.info("Applied template {} to organization: {}", cmd.templateCode(), cmd.organizationId());

        return null;
    }
}

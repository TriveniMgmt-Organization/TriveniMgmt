package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for DeleteOrganizationCommand.
 */
@Component
@Transactional
public class DeleteOrganizationHandler implements CommandHandler<DeleteOrganizationCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteOrganizationHandler.class);

    private final OrganizationRepository orgRepo;

    public DeleteOrganizationHandler(OrganizationRepository orgRepo) {
        this.orgRepo = orgRepo;
    }

    @Override
    public Void handle(DeleteOrganizationCommand cmd) {
        log.debug("Deleting organization: {}", cmd.organizationId());

        Organization org = orgRepo.findById(OrganizationId.of(cmd.organizationId()))
                .orElseThrow(() -> new OrganizationNotFoundException(OrganizationId.of(cmd.organizationId())));

        org.delete();
        orgRepo.delete(org);

        log.info("Deleted organization: {}", cmd.organizationId());

        return null;
    }
}

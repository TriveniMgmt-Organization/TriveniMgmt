package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateOrganizationCommand.
 */
@Component
@Transactional
public class UpdateOrganizationHandler implements CommandHandler<UpdateOrganizationCommand, OrganizationDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateOrganizationHandler.class);

    private final OrganizationRepository orgRepo;

    public UpdateOrganizationHandler(OrganizationRepository orgRepo) {
        this.orgRepo = orgRepo;
    }

    @Override
    public OrganizationDTO handle(UpdateOrganizationCommand cmd) {
        log.debug("Updating organization: {}", cmd.organizationId());

        Organization org = orgRepo.findById(cmd.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(cmd.organizationId()));

        org.setName(cmd.name());
        org.setDescription(cmd.description());
        org.setContactInfo(cmd.contactInfo());

        Organization saved = orgRepo.save(org);

        log.info("Updated organization: {}", saved.getId());

        return toDTO(saved);
    }

    private OrganizationDTO toDTO(Organization org) {
        return OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .description(org.getDescription())
                .contactInfo(org.getContactInfo())
                .appliedTemplateCode(org.getAppliedTemplateCode())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}

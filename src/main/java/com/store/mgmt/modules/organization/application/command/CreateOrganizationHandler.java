package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.domain.exception.DuplicateOrganizationNameException;
import com.store.mgmt.modules.organization.domain.model.ContactInfo;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.UserId;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateOrganizationCommand.
 */
@Component
@Transactional
public class CreateOrganizationHandler implements CommandHandler<CreateOrganizationCommand, OrganizationDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateOrganizationHandler.class);

    private final OrganizationRepository orgRepo;

    public CreateOrganizationHandler(OrganizationRepository orgRepo) {
        this.orgRepo = orgRepo;
    }

    @Override
    public OrganizationDTO handle(CreateOrganizationCommand cmd) {
        log.debug("Creating organization: {}", cmd.name());

        TenantContext tenant = TenantContext.current();

        // Check for duplicate name
        if (orgRepo.existsByName(cmd.name())) {
            throw new DuplicateOrganizationNameException(cmd.name());
        }

        Organization org = Organization.create(
                cmd.name(),
                cmd.description(),
                ContactInfo.of(cmd.contactInfo()),
                UserId.of(tenant.userId())
        );

        // Apply template if provided
        if (cmd.templateCode() != null && !cmd.templateCode().isBlank()) {
            org.applyTemplate(cmd.templateCode());
        }

        Organization saved = orgRepo.save(org);

        log.info("Created organization: {} with ID: {}", saved.getName(), saved.getId().getValue());

        return toDTO(saved);
    }

    private OrganizationDTO toDTO(Organization org) {
        return OrganizationDTO.builder()
                .id(org.getId().getValue())
                .name(org.getName())
                .description(org.getDescription())
                .contactInfo(org.getContactInfo() != null ? org.getContactInfo().getValue() : null)
                .appliedTemplateCode(org.getAppliedTemplateCode())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}

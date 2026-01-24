package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update an organization.
 */
public record UpdateOrganizationCommand(
        UUID organizationId,
        String name,
        String description,
        String contactInfo
) implements Command<OrganizationDTO> {}

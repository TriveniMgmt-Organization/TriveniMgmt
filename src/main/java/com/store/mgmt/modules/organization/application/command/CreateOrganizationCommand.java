package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.shared.application.command.Command;

/**
 * Command to create a new organization.
 */
public record CreateOrganizationCommand(
        String name,
        String description,
        String contactInfo,
        String templateCode
) implements Command<OrganizationDTO> {}

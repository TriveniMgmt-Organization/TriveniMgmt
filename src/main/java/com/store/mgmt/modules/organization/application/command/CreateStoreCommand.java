package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to create a new store.
 */
public record CreateStoreCommand(
        UUID organizationId,
        String name,
        String location,
        String countryCode,
        String contactInfo
) implements Command<StoreDTO> {}

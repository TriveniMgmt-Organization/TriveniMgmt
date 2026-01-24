package com.store.mgmt.modules.organization.application.command;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update a store.
 */
public record UpdateStoreCommand(
        UUID storeId,
        String name,
        String location,
        String countryCode,
        String contactInfo,
        String status
) implements Command<StoreDTO> {}

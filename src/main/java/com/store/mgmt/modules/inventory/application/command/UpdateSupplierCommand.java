package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import com.store.mgmt.shared.application.command.Command;

import java.util.UUID;

/**
 * Command to update an existing supplier.
 */
public record UpdateSupplierCommand(
        UUID id,
        UUID organizationId,
        String name,
        String contactPerson,
        String email,
        String phone,
        String address,
        String accountNumber
) implements Command<SupplierResponseDTO> {}

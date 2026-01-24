package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating a supplier.
 */
public record UpdateSupplierRequestDTO(
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        String name,

        @Size(max = 255, message = "Contact person cannot exceed 255 characters")
        String contactPerson,

        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        @Size(max = 50, message = "Phone cannot exceed 50 characters")
        String phone,

        String address,

        @Size(max = 100, message = "Account number cannot exceed 100 characters")
        String accountNumber
) {}

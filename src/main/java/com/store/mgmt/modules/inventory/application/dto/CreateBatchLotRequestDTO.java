package com.store.mgmt.modules.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a new batch/lot.
 */
public record CreateBatchLotRequestDTO(
        @NotBlank(message = "Batch number is required")
        @Size(min = 2, max = 100, message = "Batch number must be between 2 and 100 characters")
        String batchNumber,

        LocalDate manufactureDate,

        LocalDate expiryDate,

        UUID supplierId
) {}

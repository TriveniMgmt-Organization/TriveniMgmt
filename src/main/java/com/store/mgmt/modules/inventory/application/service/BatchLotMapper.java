package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.BatchLot;
import com.store.mgmt.modules.inventory.application.dto.BatchLotResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Mapper for converting BatchLot entities to DTOs.
 */
@Component
public class BatchLotMapper {

    private static final int EXPIRING_SOON_DAYS = 30;

    public BatchLotResponseDTO toResponseDTO(BatchLot batchLot) {
        if (batchLot == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        boolean isExpired = batchLot.getExpiryDate() != null && batchLot.getExpiryDate().isBefore(today);
        boolean isExpiringSoon = !isExpired && batchLot.getExpiryDate() != null &&
                batchLot.getExpiryDate().isBefore(today.plusDays(EXPIRING_SOON_DAYS));

        return BatchLotResponseDTO.builder()
                .id(batchLot.getId())
                .batchNumber(batchLot.getBatchNumber())
                .manufactureDate(batchLot.getManufactureDate())
                .expiryDate(batchLot.getExpiryDate())
                .supplierId(batchLot.getSupplier() != null ? batchLot.getSupplier().getId() : null)
                .supplierName(batchLot.getSupplier() != null ? batchLot.getSupplier().getName() : null)
                .isActive(batchLot.isActive())
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .createdAt(batchLot.getCreatedAt())
                .updatedAt(batchLot.getUpdatedAt())
                .build();
    }

    public List<BatchLotResponseDTO> toResponseDTOList(List<BatchLot> batchLots) {
        return batchLots.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.Supplier;
import com.store.mgmt.modules.inventory.application.dto.SupplierResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting Supplier entities to DTOs.
 */
@Component
public class SupplierMapper {

    public SupplierResponseDTO toResponseDTO(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        return SupplierResponseDTO.builder()
                .id(supplier.getId())
                .organizationId(supplier.getOrganizationId())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .accountNumber(supplier.getAccountNumber())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    public List<SupplierResponseDTO> toResponseDTOList(List<Supplier> suppliers) {
        return suppliers.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

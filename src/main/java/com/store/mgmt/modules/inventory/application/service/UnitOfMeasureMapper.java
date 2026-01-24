package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure;
import com.store.mgmt.modules.inventory.application.dto.UnitOfMeasureResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting UnitOfMeasure entities to DTOs.
 */
@Component
public class UnitOfMeasureMapper {

    public UnitOfMeasureResponseDTO toResponseDTO(UnitOfMeasure uom) {
        if (uom == null) {
            return null;
        }
        return UnitOfMeasureResponseDTO.builder()
                .id(uom.getId())
                .organizationId(uom.getOrganizationId())
                .name(uom.getName())
                .code(uom.getCode())
                .createdAt(uom.getCreatedAt())
                .updatedAt(uom.getUpdatedAt())
                .build();
    }

    public List<UnitOfMeasureResponseDTO> toResponseDTOList(List<UnitOfMeasure> uoms) {
        return uoms.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

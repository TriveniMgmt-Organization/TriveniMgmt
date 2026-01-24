package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.UoMConversion;
import com.store.mgmt.modules.inventory.application.dto.UoMConversionResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting UoMConversion entities to DTOs.
 */
@Component
public class UoMConversionMapper {

    public UoMConversionResponseDTO toResponseDTO(UoMConversion conversion) {
        if (conversion == null) {
            return null;
        }

        String description = String.format("1 %s = %s %s",
                conversion.getFromUom().getName(),
                conversion.getRatio().stripTrailingZeros().toPlainString(),
                conversion.getToUom().getName());

        return UoMConversionResponseDTO.builder()
                .id(conversion.getId())
                .fromUomId(conversion.getFromUom().getId())
                .fromUomName(conversion.getFromUom().getName())
                .fromUomCode(conversion.getFromUom().getCode())
                .toUomId(conversion.getToUom().getId())
                .toUomName(conversion.getToUom().getName())
                .toUomCode(conversion.getToUom().getCode())
                .ratio(conversion.getRatio())
                .description(description)
                .createdAt(conversion.getCreatedAt())
                .updatedAt(conversion.getUpdatedAt())
                .build();
    }

    public List<UoMConversionResponseDTO> toResponseDTOList(List<UoMConversion> conversions) {
        return conversions.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

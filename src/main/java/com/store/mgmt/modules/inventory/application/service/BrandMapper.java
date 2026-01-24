package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.Brand;
import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting Brand entities to DTOs.
 */
@Component
public class BrandMapper {

    public BrandResponseDTO toResponseDTO(Brand brand) {
        if (brand == null) {
            return null;
        }
        return BrandResponseDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .website(brand.getWebsite())
                .isActive(brand.isActive())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    public List<BrandResponseDTO> toResponseDTOList(List<Brand> brands) {
        return brands.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

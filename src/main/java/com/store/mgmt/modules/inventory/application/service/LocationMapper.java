package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting InventoryLocation entities to DTOs.
 */
@Component
public class LocationMapper {

    private final StoreRepository storeRepository;

    public LocationMapper(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public LocationResponseDTO toResponseDTO(InventoryLocation location) {
        if (location == null) {
            return null;
        }

        String storeName = location.getStoreId() != null
                ? storeRepository.findById(location.getStoreId()).map(s -> s.getName()).orElse(null)
                : null;

        return LocationResponseDTO.builder()
                .id(location.getId())
                .storeId(location.getStoreId())
                .storeName(storeName)
                .name(location.getName())
                .address(location.getAddress())
                .type(location.getType())
                .isActive(location.isActive())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }

    public List<LocationResponseDTO> toResponseDTOList(List<InventoryLocation> locations) {
        return locations.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.DamageLoss;
import com.store.mgmt.modules.inventory.application.dto.DamageLossResponseDTO;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting DamageLoss entities to DTOs.
 */
@Component
public class DamageLossMapper {

    private final StoreRepository storeRepository;
    private final JpaUserRepository userRepository;

    public DamageLossMapper(StoreRepository storeRepository, JpaUserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    public DamageLossResponseDTO toResponseDTO(DamageLoss damageLoss) {
        if (damageLoss == null) {
            return null;
        }

        String storeName = damageLoss.getStoreId() != null
                ? storeRepository.findById(damageLoss.getStoreId()).map(s -> s.getName()).orElse(null)
                : null;

        String userName = damageLoss.getUserId() != null
                ? userRepository.findById(damageLoss.getUserId()).map(u -> u.getUsername()).orElse(null)
                : null;

        return DamageLossResponseDTO.builder()
                .id(damageLoss.getId())
                .organizationId(damageLoss.getOrganizationId())
                .storeId(damageLoss.getStoreId())
                .storeName(storeName)
                .variantId(damageLoss.getVariant() != null ? damageLoss.getVariant().getId() : null)
                .variantSku(damageLoss.getVariant() != null ? damageLoss.getVariant().getSku() : null)
                .variantName(damageLoss.getVariant() != null ? damageLoss.getVariant().getSku() : null)
                .locationId(damageLoss.getLocation() != null ? damageLoss.getLocation().getId() : null)
                .locationName(damageLoss.getLocation() != null ? damageLoss.getLocation().getName() : null)
                .quantity(damageLoss.getQuantity())
                .reason(damageLoss.getReason() != null ? damageLoss.getReason().getValue() : null)
                .dateRecorded(damageLoss.getDateRecorded())
                .notes(damageLoss.getNotes())
                .userId(damageLoss.getUserId())
                .userName(userName)
                .createdAt(damageLoss.getCreatedAt())
                .build();
    }

    public List<DamageLossResponseDTO> toResponseDTOList(List<DamageLoss> records) {
        return records.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

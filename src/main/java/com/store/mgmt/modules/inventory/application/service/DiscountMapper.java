package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.Discount;
import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Mapper for converting Discount entities to DTOs.
 */
@Component
public class DiscountMapper {

    private final StoreRepository storeRepository;

    public DiscountMapper(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public DiscountResponseDTO toResponseDTO(Discount discount) {
        if (discount == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        boolean isCurrentlyValid = discount.isActive() &&
                !discount.getStartDate().isAfter(today) &&
                !discount.getEndDate().isBefore(today);

        String storeName = discount.getStoreId() != null
                ? storeRepository.findById(discount.getStoreId()).map(s -> s.getName()).orElse(null)
                : null;

        return DiscountResponseDTO.builder()
                .id(discount.getId())
                .organizationId(discount.getOrganizationId())
                .storeId(discount.getStoreId())
                .storeName(storeName)
                .name(discount.getName())
                .type(discount.getType() != null ? discount.getType().getValue() : null)
                .value(discount.getValue())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .productTemplateId(discount.getProductTemplate() != null ? discount.getProductTemplate().getId() : null)
                .productTemplateName(discount.getProductTemplate() != null ? discount.getProductTemplate().getName() : null)
                .categoryId(discount.getCategory() != null ? discount.getCategory().getId() : null)
                .categoryName(discount.getCategory() != null ? discount.getCategory().getName() : null)
                .description(discount.getDescription())
                .isActive(discount.isActive())
                .isCurrentlyValid(isCurrentlyValid)
                .minimumPurchaseAmount(discount.getMinimumPurchaseAmount())
                .minimumItemQuantity(discount.getMinimumItemQuantity())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .build();
    }

    public List<DiscountResponseDTO> toResponseDTOList(List<Discount> discounts) {
        return discounts.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

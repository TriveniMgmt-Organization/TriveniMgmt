package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.Sale;
import com.store.mgmt.modules.inventory.domain.model.SaleItem;
import com.store.mgmt.modules.inventory.application.dto.SaleItemResponseDTO;
import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Mapper for converting Sale entities to DTOs.
 */
@Component
public class SaleMapper {

    private final StoreRepository storeRepository;
    private final JpaUserRepository userRepository;

    public SaleMapper(StoreRepository storeRepository, JpaUserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    public SaleResponseDTO toResponseDTO(Sale sale) {
        if (sale == null) {
            return null;
        }

        List<SaleItemResponseDTO> items = sale.getSaleItems() != null
                ? sale.getSaleItems().stream()
                        .map(this::toItemResponseDTO)
                        .toList()
                : Collections.emptyList();

        String storeName = sale.getStoreId() != null
                ? storeRepository.findById(sale.getStoreId()).map(s -> s.getName()).orElse(null)
                : null;

        String userName = sale.getUserId() != null
                ? userRepository.findById(sale.getUserId()).map(u -> u.getUsername()).orElse(null)
                : null;

        return SaleResponseDTO.builder()
                .id(sale.getId())
                .storeId(sale.getStoreId())
                .storeName(storeName)
                .saleTimestamp(sale.getSaleTimestamp())
                .totalAmount(sale.getTotalAmount())
                .totalDiscountAmount(sale.getTotalDiscountAmount())
                .paymentMethod(sale.getPaymentMethod() != null ? sale.getPaymentMethod().name() : null)
                .transactionId(sale.getTransactionId())
                .userId(sale.getUserId())
                .userName(userName)
                .notes(sale.getNotes())
                .items(items)
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .build();
    }

    public SaleItemResponseDTO toItemResponseDTO(SaleItem item) {
        if (item == null) {
            return null;
        }

        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .subtract(item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO);

        return SaleItemResponseDTO.builder()
                .id(item.getId())
                .productTemplateId(item.getProductTemplate() != null ? item.getProductTemplate().getId() : null)
                .productTemplateName(item.getProductTemplate() != null ? item.getProductTemplate().getName() : null)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantSku(item.getVariant() != null ? item.getVariant().getSku() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getSku() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .lineTotal(lineTotal)
                .build();
    }

    public List<SaleResponseDTO> toResponseDTOList(List<Sale> sales) {
        return sales.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

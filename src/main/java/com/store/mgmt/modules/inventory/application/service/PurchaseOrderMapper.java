package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.PurchaseOrder;
import com.store.mgmt.modules.inventory.domain.model.PurchaseOrderItem;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderItemResponseDTO;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Mapper for converting PurchaseOrder entities to DTOs.
 */
@Component
public class PurchaseOrderMapper {

    private final StoreRepository storeRepository;
    private final JpaUserRepository userRepository;

    public PurchaseOrderMapper(StoreRepository storeRepository, JpaUserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    public PurchaseOrderResponseDTO toResponseDTO(PurchaseOrder po) {
        if (po == null) {
            return null;
        }

        List<PurchaseOrderItemResponseDTO> items = po.getPurchaseOrderItems() != null
                ? po.getPurchaseOrderItems().stream()
                        .map(this::toItemResponseDTO)
                        .toList()
                : Collections.emptyList();

        String userName = po.getUserId() != null
                ? userRepository.findById(po.getUserId()).map(u -> u.getUsername()).orElse(null)
                : null;

        return PurchaseOrderResponseDTO.builder()
                .id(po.getId())
                .organizationId(po.getOrganizationId())
                .supplierId(po.getSupplier() != null ? po.getSupplier().getId() : null)
                .supplierName(po.getSupplier() != null ? po.getSupplier().getName() : null)
                .orderDate(po.getOrderDate())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .actualDeliveryDate(po.getActualDeliveryDate())
                .status(po.getStatus() != null ? po.getStatus().getValue() : null)
                .totalEstimatedAmount(po.getTotalEstimatedAmount())
                .trackingNumber(po.getTrackingNumber())
                .notes(po.getNotes())
                .userId(po.getUserId())
                .userName(userName)
                .items(items)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public PurchaseOrderItemResponseDTO toItemResponseDTO(PurchaseOrderItem item) {
        if (item == null) {
            return null;
        }

        int pendingQuantity = item.getOrderedQuantity() - item.getReceivedQuantity();
        BigDecimal totalCost = item.getUnitCost().multiply(BigDecimal.valueOf(item.getOrderedQuantity()));

        String storeName = item.getStoreId() != null
                ? storeRepository.findById(item.getStoreId()).map(s -> s.getName()).orElse(null)
                : null;

        return PurchaseOrderItemResponseDTO.builder()
                .id(item.getId())
                .storeId(item.getStoreId())
                .storeName(storeName)
                .productTemplateId(item.getProductTemplate() != null ? item.getProductTemplate().getId() : null)
                .productTemplateName(item.getProductTemplate() != null ? item.getProductTemplate().getName() : null)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantSku(item.getVariant() != null ? item.getVariant().getSku() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getSku() : null)
                .orderedQuantity(item.getOrderedQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .pendingQuantity(pendingQuantity)
                .unitCost(item.getUnitCost())
                .totalCost(totalCost)
                .build();
    }

    public List<PurchaseOrderResponseDTO> toResponseDTOList(List<PurchaseOrder> orders) {
        return orders.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

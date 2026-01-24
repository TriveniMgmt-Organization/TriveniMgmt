package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.StockTransaction;
import com.store.mgmt.modules.inventory.application.dto.StockTransactionResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting StockTransaction entities to DTOs.
 */
@Component
public class StockTransactionMapper {

    public StockTransactionResponseDTO toResponseDTO(StockTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        InventoryItem item = transaction.getInventoryItem();

        return StockTransactionResponseDTO.builder()
                .id(transaction.getId())
                .inventoryItemId(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantSku(item.getVariant() != null ? item.getVariant().getSku() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getSku() : null)
                .locationId(item.getLocation() != null ? item.getLocation().getId() : null)
                .locationName(item.getLocation() != null ? item.getLocation().getName() : null)
                .type(transaction.getType().name())
                .quantityDelta(transaction.getQuantityDelta())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .reason(transaction.getReason() != null ? transaction.getReason().name() : null)
                .fromLocationId(transaction.getFromLocation() != null ? transaction.getFromLocation().getId() : null)
                .fromLocationName(transaction.getFromLocation() != null ? transaction.getFromLocation().getName() : null)
                .toLocationId(transaction.getToLocation() != null ? transaction.getToLocation().getId() : null)
                .toLocationName(transaction.getToLocation() != null ? transaction.getToLocation().getName() : null)
                .userId(transaction.getUserId())
                .userName(null)
                .timestamp(transaction.getTimestamp())
                .notes(transaction.getNotes())
                .build();
    }

    public List<StockTransactionResponseDTO> toResponseDTOList(List<StockTransaction> transactions) {
        return transactions.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

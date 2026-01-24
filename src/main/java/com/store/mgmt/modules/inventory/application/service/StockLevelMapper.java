package com.store.mgmt.modules.inventory.application.service;

import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.application.dto.StockLevelResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting StockLevel entities to DTOs.
 */
@Component
public class StockLevelMapper {

    public StockLevelResponseDTO toResponseDTO(StockLevel stockLevel) {
        if (stockLevel == null) {
            return null;
        }

        InventoryItem item = stockLevel.getInventoryItem();
        boolean isLowStock = stockLevel.getAvailable() < stockLevel.getLowStockThreshold();

        return StockLevelResponseDTO.builder()
                .id(stockLevel.getId())
                .inventoryItemId(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantSku(item.getVariant() != null ? item.getVariant().getSku() : null)
                .variantName(item.getVariant() != null ? item.getVariant().getSku() : null)
                .locationId(item.getLocation() != null ? item.getLocation().getId() : null)
                .locationName(item.getLocation() != null ? item.getLocation().getName() : null)
                .onHand(stockLevel.getOnHand())
                .committed(stockLevel.getCommitted())
                .available(stockLevel.getAvailable())
                .lowStockThreshold(stockLevel.getLowStockThreshold())
                .maxStockLevel(stockLevel.getMaxStockLevel())
                .isLowStock(isLowStock)
                .updatedAt(stockLevel.getUpdatedAt())
                .build();
    }

    public List<StockLevelResponseDTO> toResponseDTOList(List<StockLevel> stockLevels) {
        return stockLevels.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}

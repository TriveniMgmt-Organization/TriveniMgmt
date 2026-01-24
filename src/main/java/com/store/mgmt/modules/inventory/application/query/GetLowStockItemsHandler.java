package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.InventoryItem;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.application.dto.LowStockItemResponseDTO;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetLowStockItemsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetLowStockItemsHandler implements QueryHandler<GetLowStockItemsQuery, List<LowStockItemResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetLowStockItemsHandler.class);

    private final StockLevelRepository stockLevelRepository;

    public GetLowStockItemsHandler(StockLevelRepository stockLevelRepository) {
        this.stockLevelRepository = stockLevelRepository;
    }

    @Override
    public List<LowStockItemResponseDTO> handle(GetLowStockItemsQuery query) {
        log.debug("Getting low stock items, locationId: {}", query.locationId());

        List<StockLevel> lowStockItems;
        if (query.locationId() != null) {
            lowStockItems = stockLevelRepository.findLowStockItemsByLocationId(query.locationId());
        } else {
            lowStockItems = stockLevelRepository.findLowStockItems();
        }

        return lowStockItems.stream()
                .map(this::toDTO)
                .toList();
    }

    private LowStockItemResponseDTO toDTO(StockLevel stockLevel) {
        InventoryItem item = stockLevel.getInventoryItem();
        int shortfall = stockLevel.getLowStockThreshold() - stockLevel.getAvailable();

        return LowStockItemResponseDTO.builder()
                .inventoryItemId(item.getId())
                .variantId(item.getVariant().getId())
                .variantSku(item.getVariant().getSku())
                .variantName(item.getVariant().getSku())
                .locationId(item.getLocation().getId())
                .locationName(item.getLocation().getName())
                .onHand(stockLevel.getOnHand())
                .available(stockLevel.getAvailable())
                .lowStockThreshold(stockLevel.getLowStockThreshold())
                .shortfall(shortfall > 0 ? shortfall : 0)
                .expiryDate(item.getExpiryDate())
                .build();
    }
}

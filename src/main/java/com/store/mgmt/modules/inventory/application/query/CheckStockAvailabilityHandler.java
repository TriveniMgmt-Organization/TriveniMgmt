package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.ProductVariant;
import com.store.mgmt.modules.inventory.domain.repository.ProductVariantRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.application.dto.StockAvailabilityResponseDTO;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CheckStockAvailabilityQuery.
 */
@Component
@Transactional(readOnly = true)
public class CheckStockAvailabilityHandler implements QueryHandler<CheckStockAvailabilityQuery, StockAvailabilityResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CheckStockAvailabilityHandler.class);

    private final StockLevelRepository stockLevelRepository;
    private final ProductVariantRepository variantRepository;

    public CheckStockAvailabilityHandler(
            StockLevelRepository stockLevelRepository,
            ProductVariantRepository variantRepository
    ) {
        this.stockLevelRepository = stockLevelRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public StockAvailabilityResponseDTO handle(CheckStockAvailabilityQuery query) {
        log.debug("Checking stock availability for variant: {}, requested: {}",
                query.variantId(), query.requestedQuantity());

        ProductVariant variant = variantRepository.findById(query.variantId())
                .orElseThrow(() -> new EntityNotFoundException("Variant not found with ID: " + query.variantId()));

        Integer totalAvailable = stockLevelRepository.getTotalAvailableByVariantId(query.variantId());
        if (totalAvailable == null) {
            totalAvailable = 0;
        }

        boolean isAvailable = totalAvailable >= query.requestedQuantity();
        int shortfall = isAvailable ? 0 : query.requestedQuantity() - totalAvailable;

        return StockAvailabilityResponseDTO.builder()
                .variantId(variant.getId())
                .variantSku(variant.getSku())
                .variantName(variant.getSku())
                .requestedQuantity(query.requestedQuantity())
                .availableQuantity(totalAvailable)
                .isAvailable(isAvailable)
                .shortfall(shortfall)
                .build();
    }
}

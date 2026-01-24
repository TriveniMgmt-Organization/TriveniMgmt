package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.ProductVariant;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.ProductVariantRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.application.dto.StockSummaryResponseDTO;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetTotalStockByVariantQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetTotalStockByVariantHandler implements QueryHandler<GetTotalStockByVariantQuery, StockSummaryResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetTotalStockByVariantHandler.class);

    private final StockLevelRepository stockLevelRepository;
    private final ProductVariantRepository variantRepository;

    public GetTotalStockByVariantHandler(
            StockLevelRepository stockLevelRepository,
            ProductVariantRepository variantRepository
    ) {
        this.stockLevelRepository = stockLevelRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public StockSummaryResponseDTO handle(GetTotalStockByVariantQuery query) {
        log.debug("Getting total stock for variant: {}", query.variantId());

        ProductVariant variant = variantRepository.findById(query.variantId())
                .orElseThrow(() -> new EntityNotFoundException("Variant not found with ID: " + query.variantId()));

        List<StockLevel> stockLevels = stockLevelRepository.findByVariantId(query.variantId());

        int totalOnHand = stockLevels.stream().mapToInt(StockLevel::getOnHand).sum();
        int totalCommitted = stockLevels.stream().mapToInt(StockLevel::getCommitted).sum();
        int totalAvailable = stockLevels.stream().mapToInt(StockLevel::getAvailable).sum();

        return StockSummaryResponseDTO.builder()
                .variantId(variant.getId())
                .variantSku(variant.getSku())
                .variantName(variant.getSku()) // Variants use SKU as identifier
                .templateId(variant.getTemplate() != null ? variant.getTemplate().getId() : null)
                .templateName(variant.getTemplate() != null ? variant.getTemplate().getName() : null)
                .totalOnHand(totalOnHand)
                .totalCommitted(totalCommitted)
                .totalAvailable(totalAvailable)
                .locationCount(stockLevels.size())
                .build();
    }
}

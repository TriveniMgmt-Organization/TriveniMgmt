package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.ProductTemplate;
import com.store.mgmt.modules.inventory.domain.model.ProductVariant;
import com.store.mgmt.modules.inventory.domain.model.StockLevel;
import com.store.mgmt.modules.inventory.domain.repository.ProductTemplateRepository;
import com.store.mgmt.modules.inventory.domain.repository.ProductVariantRepository;
import com.store.mgmt.modules.inventory.domain.repository.StockLevelRepository;
import com.store.mgmt.modules.inventory.application.dto.StockSummaryResponseDTO;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for GetTotalStockByTemplateQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetTotalStockByTemplateHandler implements QueryHandler<GetTotalStockByTemplateQuery, List<StockSummaryResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetTotalStockByTemplateHandler.class);

    private final StockLevelRepository stockLevelRepository;
    private final ProductTemplateRepository templateRepository;
    private final ProductVariantRepository variantRepository;

    public GetTotalStockByTemplateHandler(
            StockLevelRepository stockLevelRepository,
            ProductTemplateRepository templateRepository,
            ProductVariantRepository variantRepository
    ) {
        this.stockLevelRepository = stockLevelRepository;
        this.templateRepository = templateRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public List<StockSummaryResponseDTO> handle(GetTotalStockByTemplateQuery query) {
        log.debug("Getting total stock for template: {}", query.templateId());

        ProductTemplate template = templateRepository.findById(query.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Template not found with ID: " + query.templateId()));

        List<ProductVariant> variants = variantRepository.findByTemplateId(query.templateId());
        List<StockSummaryResponseDTO> summaries = new ArrayList<>();

        for (ProductVariant variant : variants) {
            List<StockLevel> stockLevels = stockLevelRepository.findByVariantId(variant.getId());

            int totalOnHand = stockLevels.stream().mapToInt(StockLevel::getOnHand).sum();
            int totalCommitted = stockLevels.stream().mapToInt(StockLevel::getCommitted).sum();
            int totalAvailable = stockLevels.stream().mapToInt(StockLevel::getAvailable).sum();

            summaries.add(StockSummaryResponseDTO.builder()
                    .variantId(variant.getId())
                    .variantSku(variant.getSku())
                    .variantName(variant.getSku())
                    .templateId(template.getId())
                    .templateName(template.getName())
                    .totalOnHand(totalOnHand)
                    .totalCommitted(totalCommitted)
                    .totalAvailable(totalAvailable)
                    .locationCount(stockLevels.size())
                    .build());
        }

        return summaries;
    }
}

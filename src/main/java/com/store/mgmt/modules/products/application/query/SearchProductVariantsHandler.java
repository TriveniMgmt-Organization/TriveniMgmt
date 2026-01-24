package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Handler for SearchProductVariantsQuery.
 * Searches by SKU or barcode.
 */
@Component
@Transactional(readOnly = true)
public class SearchProductVariantsHandler implements QueryHandler<SearchProductVariantsQuery, ProductVariantDTO> {

    private static final Logger log = LoggerFactory.getLogger(SearchProductVariantsHandler.class);

    private final ProductVariantRepository variantRepo;

    public SearchProductVariantsHandler(ProductVariantRepository variantRepo) {
        this.variantRepo = variantRepo;
    }

    @Override
    public ProductVariantDTO handle(SearchProductVariantsQuery query) {
        log.debug("Searching product variant, sku: {}, barcode: {}", query.sku(), query.barcode());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        Optional<ProductVariant> variant = Optional.empty();

        // Search by SKU first
        if (query.sku() != null && !query.sku().isBlank()) {
            try {
                Sku sku = Sku.of(query.sku());
                variant = variantRepo.findBySkuAndOrganizationId(sku, orgId);
            } catch (IllegalArgumentException e) {
                log.debug("Invalid SKU format: {}", query.sku());
            }
        }

        // If not found by SKU, try barcode
        if (variant.isEmpty() && query.barcode() != null && !query.barcode().isBlank()) {
            Barcode barcode = Barcode.of(query.barcode());
            if (barcode != null) {
                variant = variantRepo.findByBarcodeAndOrganizationId(barcode, orgId);
            }
        }

        return variant.map(this::toDTO).orElse(null);
    }

    private ProductVariantDTO toDTO(ProductVariant variant) {
        return ProductVariantDTO.builder()
                .id(variant.getId().getValue())
                .templateId(variant.getTemplateId().getValue())
                .sku(variant.getSku().getValue())
                .barcode(variant.getBarcode() != null ? variant.getBarcode().getValue() : null)
                .costPrice(variant.getCostPrice().getAmount())
                .retailPrice(variant.getRetailPrice().getAmount())
                .margin(variant.calculateMargin().getAmount())
                .attributeValues(variant.getAttributeValues().getAll())
                .active(variant.isActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}

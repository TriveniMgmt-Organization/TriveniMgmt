package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.domain.exception.ProductVariantNotFoundException;
import com.store.mgmt.modules.products.domain.model.OrganizationId;
import com.store.mgmt.modules.products.domain.model.ProductVariant;
import com.store.mgmt.modules.products.domain.model.ProductVariantId;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetProductVariantQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetProductVariantHandler implements QueryHandler<GetProductVariantQuery, ProductVariantDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetProductVariantHandler.class);

    private final ProductVariantRepository variantRepo;

    public GetProductVariantHandler(ProductVariantRepository variantRepo) {
        this.variantRepo = variantRepo;
    }

    @Override
    public ProductVariantDTO handle(GetProductVariantQuery query) {
        log.debug("Getting product variant: {}", query.variantId());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        ProductVariant variant = variantRepo.findByIdAndOrganizationId(
                ProductVariantId.of(query.variantId()),
                orgId
        ).orElseThrow(() -> new ProductVariantNotFoundException(ProductVariantId.of(query.variantId())));

        return toDTO(variant);
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

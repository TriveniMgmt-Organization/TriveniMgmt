package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.domain.exception.ProductTemplateNotFoundException;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetProductTemplateQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetProductTemplateHandler implements QueryHandler<GetProductTemplateQuery, ProductTemplateDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetProductTemplateHandler.class);

    private final ProductTemplateRepository templateRepo;
    private final ProductVariantRepository variantRepo;

    public GetProductTemplateHandler(
            ProductTemplateRepository templateRepo,
            ProductVariantRepository variantRepo
    ) {
        this.templateRepo = templateRepo;
        this.variantRepo = variantRepo;
    }

    @Override
    public ProductTemplateDTO handle(GetProductTemplateQuery query) {
        log.debug("Getting product template: {}", query.templateId());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        ProductTemplate template = templateRepo.findByIdAndOrganizationId(
                ProductTemplateId.of(query.templateId()),
                orgId
        ).orElseThrow(() -> new ProductTemplateNotFoundException(ProductTemplateId.of(query.templateId())));

        // Load variants
        List<ProductVariant> variants = variantRepo.findByTemplateId(template.getId());

        return toDTO(template, variants);
    }

    private ProductTemplateDTO toDTO(ProductTemplate template, List<ProductVariant> variants) {
        List<ProductVariantDTO> variantDTOs = variants.stream()
                .map(this::toVariantDTO)
                .collect(Collectors.toList());

        return ProductTemplateDTO.builder()
                .id(template.getId().getValue())
                .name(template.getName())
                .description(template.getDescription())
                .categoryId(template.getCategoryId().getValue())
                .brandId(template.getBrandId() != null ? template.getBrandId().getValue() : null)
                .unitOfMeasureId(template.getUnitOfMeasureId().getValue())
                .imageUrl(template.getImageUrl())
                .reorderPoint(template.getReorderPoint())
                .requiresExpiry(template.isRequiresExpiry())
                .active(template.isActive())
                .attributes(template.getAttributes().getAll())
                .variants(variantDTOs)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private ProductVariantDTO toVariantDTO(ProductVariant variant) {
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

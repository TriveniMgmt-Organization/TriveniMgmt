package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.domain.model.OrganizationId;
import com.store.mgmt.modules.products.domain.model.ProductTemplate;
import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.modules.products.domain.model.ProductVariant;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler for GetProductVariantsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetProductVariantsHandler implements QueryHandler<GetProductVariantsQuery, List<ProductVariantDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetProductVariantsHandler.class);

    private final ProductVariantRepository variantRepo;
    private final ProductTemplateRepository templateRepo;

    public GetProductVariantsHandler(ProductVariantRepository variantRepo, ProductTemplateRepository templateRepo) {
        this.variantRepo = variantRepo;
        this.templateRepo = templateRepo;
    }

    @Override
    public List<ProductVariantDTO> handle(GetProductVariantsQuery query) {
        log.debug("Getting product variants, templateId: {}, activeOnly: {}", query.templateId(), query.activeOnly());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        List<ProductVariant> variants;

        if (query.templateId() != null) {
            if (query.activeOnly()) {
                variants = variantRepo.findActiveByTemplateId(ProductTemplateId.of(query.templateId()));
            } else {
                variants = variantRepo.findByTemplateId(ProductTemplateId.of(query.templateId()));
            }
        } else {
            variants = variantRepo.findByOrganizationId(orgId);
        }

        // Simple pagination
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), variants.size());

        if (start >= variants.size()) {
            return List.of();
        }

        List<ProductVariant> paginatedVariants = variants.subList(start, end);

        // Fetch template names for all variants in a single query
        List<UUID> templateIds = paginatedVariants.stream()
                .map(v -> v.getTemplateId().getValue())
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, String> templateNameMap = templateRepo.findByOrganizationId(orgId).stream()
                .filter(t -> templateIds.contains(t.getId().getValue()))
                .collect(Collectors.toMap(
                        t -> t.getId().getValue(),
                        ProductTemplate::getName
                ));

        return paginatedVariants.stream()
                .map(v -> toDTO(v, templateNameMap.get(v.getTemplateId().getValue())))
                .collect(Collectors.toList());
    }

    private ProductVariantDTO toDTO(ProductVariant variant, String templateName) {
        return ProductVariantDTO.builder()
                .id(variant.getId().getValue())
                .templateId(variant.getTemplateId().getValue())
                .templateName(templateName)
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

package com.store.mgmt.modules.products.application.query;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetProductTemplatesQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetProductTemplatesHandler implements QueryHandler<GetProductTemplatesQuery, List<ProductTemplateDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetProductTemplatesHandler.class);

    private final ProductTemplateRepository templateRepo;

    public GetProductTemplatesHandler(ProductTemplateRepository templateRepo) {
        this.templateRepo = templateRepo;
    }

    @Override
    public List<ProductTemplateDTO> handle(GetProductTemplatesQuery query) {
        log.debug("Getting product templates, categoryId: {}, activeOnly: {}", query.categoryId(), query.activeOnly());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        List<ProductTemplate> templates;

        if (query.categoryId() != null) {
            templates = templateRepo.findByCategoryIdAndOrganizationId(
                    CategoryId.of(query.categoryId()),
                    orgId
            );
        } else if (query.activeOnly()) {
            templates = templateRepo.findActiveByOrganizationId(orgId);
        } else {
            templates = templateRepo.findByOrganizationId(orgId);
        }

        // Simple pagination (in production, use repository-level pagination)
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), templates.size());

        if (start >= templates.size()) {
            return List.of();
        }

        return templates.subList(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ProductTemplateDTO toDTO(ProductTemplate template) {
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
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.modules.products.domain.exception.ProductTemplateNotFoundException;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateProductTemplateCommand.
 */
@Component
@Transactional
public class UpdateProductTemplateHandler implements CommandHandler<UpdateProductTemplateCommand, ProductTemplateDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateProductTemplateHandler.class);

    private final ProductTemplateRepository templateRepo;

    public UpdateProductTemplateHandler(ProductTemplateRepository templateRepo) {
        this.templateRepo = templateRepo;
    }

    @Override
    public ProductTemplateDTO handle(UpdateProductTemplateCommand cmd) {
        log.debug("Updating product template: {}", cmd.templateId());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        ProductTemplate template = templateRepo.findByIdAndOrganizationId(
                ProductTemplateId.of(cmd.templateId()),
                orgId
        ).orElseThrow(() -> new ProductTemplateNotFoundException(ProductTemplateId.of(cmd.templateId())));

        template.updateDetails(
                cmd.name(),
                cmd.description(),
                cmd.categoryId() != null ? CategoryId.of(cmd.categoryId()) : null,
                cmd.unitOfMeasureId() != null ? UnitOfMeasureId.of(cmd.unitOfMeasureId()) : null,
                cmd.brandId() != null ? BrandId.of(cmd.brandId()) : null,
                cmd.imageUrl(),
                cmd.reorderPoint(),
                cmd.requiresExpiry()
        );

        if (cmd.attributes() != null) {
            template.updateAttributes(ProductAttributes.of(cmd.attributes()));
        }

        ProductTemplate saved = templateRepo.save(template);

        log.info("Updated product template: {}", saved.getId().getValue());

        return toDTO(saved);
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

package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.application.dto.ProductTemplateDTO;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateProductTemplateCommand.
 */
@Component
@Transactional
public class CreateProductTemplateHandler implements CommandHandler<CreateProductTemplateCommand, ProductTemplateDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateProductTemplateHandler.class);

    private final ProductTemplateRepository templateRepo;

    public CreateProductTemplateHandler(ProductTemplateRepository templateRepo) {
        this.templateRepo = templateRepo;
    }

    @Override
    public ProductTemplateDTO handle(CreateProductTemplateCommand cmd) {
        log.debug("Creating product template: {}", cmd.name());

        TenantContext.current(); // Validates tenant context is set

        ProductTemplate template = ProductTemplate.create(
                cmd.name(),
                cmd.description(),
                CategoryId.of(cmd.categoryId()),
                UnitOfMeasureId.of(cmd.unitOfMeasureId()),
                cmd.brandId() != null ? BrandId.of(cmd.brandId()) : null,
                cmd.imageUrl(),
                cmd.reorderPoint(),
                cmd.requiresExpiry(),
                ProductAttributes.of(cmd.attributes())
        );

        ProductTemplate saved = templateRepo.save(template);

        log.info("Created product template: {} with ID: {}", saved.getName(), saved.getId().getValue());

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

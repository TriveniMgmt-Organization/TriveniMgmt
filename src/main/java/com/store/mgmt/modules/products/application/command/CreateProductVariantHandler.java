package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.domain.exception.DuplicateBarcodeException;
import com.store.mgmt.modules.products.domain.exception.DuplicateSkuException;
import com.store.mgmt.modules.products.domain.exception.ProductTemplateNotFoundException;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateProductVariantCommand.
 */
@Component
@Transactional
public class CreateProductVariantHandler implements CommandHandler<CreateProductVariantCommand, ProductVariantDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateProductVariantHandler.class);

    private final ProductVariantRepository variantRepo;
    private final ProductTemplateRepository templateRepo;

    public CreateProductVariantHandler(
            ProductVariantRepository variantRepo,
            ProductTemplateRepository templateRepo
    ) {
        this.variantRepo = variantRepo;
        this.templateRepo = templateRepo;
    }

    @Override
    public ProductVariantDTO handle(CreateProductVariantCommand cmd) {
        log.debug("Creating product variant for template: {}", cmd.templateId());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        // Validate template exists
        ProductTemplateId templateId = ProductTemplateId.of(cmd.templateId());
        templateRepo.findByIdAndOrganizationId(templateId, orgId)
                .orElseThrow(() -> new ProductTemplateNotFoundException(templateId));

        Sku sku = Sku.of(cmd.sku());
        Barcode barcode = Barcode.of(cmd.barcode());

        // Check for duplicate SKU
        if (variantRepo.existsBySkuAndOrganizationId(sku, orgId)) {
            throw new DuplicateSkuException(sku);
        }

        // Check for duplicate barcode
        if (barcode != null && variantRepo.existsByBarcodeAndOrganizationId(barcode, orgId)) {
            throw new DuplicateBarcodeException(barcode);
        }

        ProductVariant variant = ProductVariant.create(
                templateId,
                sku,
                barcode,
                Money.of(cmd.costPrice()),
                Money.of(cmd.retailPrice()),
                ProductAttributes.of(cmd.attributeValues())
        );

        ProductVariant saved = variantRepo.save(variant);

        log.info("Created product variant: {} with SKU: {}", saved.getId().getValue(), saved.getSku().getValue());

        return toDTO(saved);
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

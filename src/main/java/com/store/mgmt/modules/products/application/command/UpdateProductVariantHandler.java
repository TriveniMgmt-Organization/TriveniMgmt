package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.application.dto.ProductVariantDTO;
import com.store.mgmt.modules.products.domain.exception.DuplicateBarcodeException;
import com.store.mgmt.modules.products.domain.exception.DuplicateSkuException;
import com.store.mgmt.modules.products.domain.exception.ProductVariantNotFoundException;
import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateProductVariantCommand.
 */
@Component
@Transactional
public class UpdateProductVariantHandler implements CommandHandler<UpdateProductVariantCommand, ProductVariantDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateProductVariantHandler.class);

    private final ProductVariantRepository variantRepo;

    public UpdateProductVariantHandler(ProductVariantRepository variantRepo) {
        this.variantRepo = variantRepo;
    }

    @Override
    public ProductVariantDTO handle(UpdateProductVariantCommand cmd) {
        log.debug("Updating product variant: {}", cmd.variantId());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        ProductVariant variant = variantRepo.findByIdAndOrganizationId(
                ProductVariantId.of(cmd.variantId()),
                orgId
        ).orElseThrow(() -> new ProductVariantNotFoundException(ProductVariantId.of(cmd.variantId())));

        // Update SKU if provided
        if (cmd.sku() != null) {
            Sku newSku = Sku.of(cmd.sku());
            if (!newSku.equals(variant.getSku()) && variantRepo.existsBySkuAndOrganizationId(newSku, orgId)) {
                throw new DuplicateSkuException(newSku);
            }
            variant.updateSku(newSku);
        }

        // Update barcode if provided
        if (cmd.barcode() != null) {
            Barcode newBarcode = Barcode.of(cmd.barcode());
            if (newBarcode != null && !newBarcode.equals(variant.getBarcode()) &&
                    variantRepo.existsByBarcodeAndOrganizationId(newBarcode, orgId)) {
                throw new DuplicateBarcodeException(newBarcode);
            }
            variant.updateBarcode(newBarcode);
        }

        // Update prices if provided
        if (cmd.costPrice() != null && cmd.retailPrice() != null) {
            variant.updatePrices(Money.of(cmd.costPrice()), Money.of(cmd.retailPrice()));
        }

        // Update attribute values if provided
        if (cmd.attributeValues() != null) {
            variant.updateAttributeValues(ProductAttributes.of(cmd.attributeValues()));
        }

        // Update active status if provided
        if (cmd.active() != null) {
            if (cmd.active()) {
                variant.activate();
            } else {
                variant.deactivate();
            }
        }

        ProductVariant saved = variantRepo.save(variant);

        log.info("Updated product variant: {}", saved.getId().getValue());

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

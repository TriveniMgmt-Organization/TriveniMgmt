package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.domain.exception.ProductVariantNotFoundException;
import com.store.mgmt.modules.products.domain.model.OrganizationId;
import com.store.mgmt.modules.products.domain.model.ProductVariant;
import com.store.mgmt.modules.products.domain.model.ProductVariantId;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for DeleteProductVariantCommand.
 */
@Component
@Transactional
public class DeleteProductVariantHandler implements CommandHandler<DeleteProductVariantCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteProductVariantHandler.class);

    private final ProductVariantRepository variantRepo;

    public DeleteProductVariantHandler(ProductVariantRepository variantRepo) {
        this.variantRepo = variantRepo;
    }

    @Override
    public Void handle(DeleteProductVariantCommand cmd) {
        log.debug("Deleting product variant: {}", cmd.variantId());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        ProductVariant variant = variantRepo.findByIdAndOrganizationId(
                ProductVariantId.of(cmd.variantId()),
                orgId
        ).orElseThrow(() -> new ProductVariantNotFoundException(ProductVariantId.of(cmd.variantId())));

        variant.delete();
        variantRepo.delete(variant);

        log.info("Deleted product variant: {}", cmd.variantId());

        return null;
    }
}

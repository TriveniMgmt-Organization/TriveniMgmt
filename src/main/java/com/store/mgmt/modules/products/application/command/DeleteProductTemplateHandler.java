package com.store.mgmt.modules.products.application.command;

import com.store.mgmt.modules.products.domain.exception.ProductTemplateNotFoundException;
import com.store.mgmt.modules.products.domain.model.OrganizationId;
import com.store.mgmt.modules.products.domain.model.ProductTemplate;
import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for DeleteProductTemplateCommand.
 */
@Component
@Transactional
public class DeleteProductTemplateHandler implements CommandHandler<DeleteProductTemplateCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteProductTemplateHandler.class);

    private final ProductTemplateRepository templateRepo;

    public DeleteProductTemplateHandler(ProductTemplateRepository templateRepo) {
        this.templateRepo = templateRepo;
    }

    @Override
    public Void handle(DeleteProductTemplateCommand cmd) {
        log.debug("Deleting product template: {}", cmd.templateId());

        TenantContext tenant = TenantContext.current();
        OrganizationId orgId = OrganizationId.of(tenant.organizationId());

        ProductTemplate template = templateRepo.findByIdAndOrganizationId(
                ProductTemplateId.of(cmd.templateId()),
                orgId
        ).orElseThrow(() -> new ProductTemplateNotFoundException(ProductTemplateId.of(cmd.templateId())));

        template.delete();
        templateRepo.delete(template);

        log.info("Deleted product template: {}", cmd.templateId());

        return null;
    }
}

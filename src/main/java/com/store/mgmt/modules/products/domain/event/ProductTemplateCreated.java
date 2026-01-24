package com.store.mgmt.modules.products.domain.event;

import com.store.mgmt.modules.products.domain.model.CategoryId;
import com.store.mgmt.modules.products.domain.model.OrganizationId;
import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a new product template is created.
 */
public final class ProductTemplateCreated extends BaseDomainEvent {

    private final ProductTemplateId templateId;
    private final String name;
    private final CategoryId categoryId;
    private final OrganizationId organizationId;

    public ProductTemplateCreated(
            ProductTemplateId templateId,
            String name,
            CategoryId categoryId,
            OrganizationId organizationId
    ) {
        super(templateId.getValue(), "ProductTemplate");
        this.templateId = templateId;
        this.name = name;
        this.categoryId = categoryId;
        this.organizationId = organizationId;
    }

    public ProductTemplateId getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }
}

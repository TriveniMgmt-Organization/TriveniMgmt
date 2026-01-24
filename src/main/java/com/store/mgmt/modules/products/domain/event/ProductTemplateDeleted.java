package com.store.mgmt.modules.products.domain.event;

import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a product template is soft-deleted.
 */
public final class ProductTemplateDeleted extends BaseDomainEvent {

    private final ProductTemplateId templateId;

    public ProductTemplateDeleted(ProductTemplateId templateId) {
        super(templateId.getValue(), "ProductTemplate");
        this.templateId = templateId;
    }

    public ProductTemplateId getTemplateId() {
        return templateId;
    }
}

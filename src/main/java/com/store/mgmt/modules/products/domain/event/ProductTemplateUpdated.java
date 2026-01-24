package com.store.mgmt.modules.products.domain.event;

import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.shared.domain.event.BaseDomainEvent;

/**
 * Event raised when a product template is updated.
 */
public final class ProductTemplateUpdated extends BaseDomainEvent {

    private final ProductTemplateId templateId;
    private final String updatedFields;

    public ProductTemplateUpdated(ProductTemplateId templateId, String updatedFields) {
        super(templateId.getValue(), "ProductTemplate");
        this.templateId = templateId;
        this.updatedFields = updatedFields;
    }

    public ProductTemplateId getTemplateId() {
        return templateId;
    }

    public String getUpdatedFields() {
        return updatedFields;
    }
}

package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to apply a template to an organization that already has one.
 */
public class TemplateAlreadyAppliedException extends DomainException {

    public TemplateAlreadyAppliedException(String currentTemplate) {
        super("Template already applied to this organization: " + currentTemplate);
    }
}

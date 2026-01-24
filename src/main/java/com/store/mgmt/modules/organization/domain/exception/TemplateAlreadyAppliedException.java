package com.store.mgmt.modules.organization.domain.exception;

import com.store.mgmt.shared.domain.exception.DomainException;

import java.util.UUID;

/**
 * Exception thrown when attempting to apply a template to an organization that already has one.
 */
public class TemplateAlreadyAppliedException extends DomainException {

    public TemplateAlreadyAppliedException(UUID organizationId, String currentTemplate) {
        super("Template already applied to organization " + organizationId + ": " + currentTemplate);
    }

    public TemplateAlreadyAppliedException(String currentTemplate) {
        super("Template already applied to this organization: " + currentTemplate);
    }
}

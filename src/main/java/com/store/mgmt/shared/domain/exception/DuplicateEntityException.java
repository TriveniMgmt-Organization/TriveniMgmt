package com.store.mgmt.shared.domain.exception;

/**
 * Exception thrown when trying to create a duplicate entity.
 */
public class DuplicateEntityException extends DomainException {

    private final String entityType;
    private final String identifier;

    public DuplicateEntityException(String entityType, String identifier) {
        super(String.format("%s with identifier '%s' already exists", entityType, identifier));
        this.entityType = entityType;
        this.identifier = identifier;
    }

    public DuplicateEntityException(String message) {
        super(message);
        this.entityType = null;
        this.identifier = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getIdentifier() {
        return identifier;
    }
}

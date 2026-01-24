package com.store.mgmt.shared.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when an entity is not found.
 */
public class EntityNotFoundException extends DomainException {

    private final String entityType;
    private final UUID entityId;

    public EntityNotFoundException(String entityType, UUID entityId) {
        super(String.format("%s with ID %s not found", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityType, String identifier) {
        super(String.format("%s '%s' not found", entityType, identifier));
        this.entityType = entityType;
        this.entityId = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }
}

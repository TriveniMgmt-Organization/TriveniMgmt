package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.shared.domain.model.Identifier;

import java.util.UUID;

/**
 * Strongly-typed identifier for Organization.
 */
public final class OrganizationId extends Identifier {

    private OrganizationId(UUID value) {
        super(value);
    }

    public static OrganizationId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("OrganizationId cannot be null");
        }
        return new OrganizationId(value);
    }

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID());
    }
}

package com.store.mgmt.shared.domain.model;

import java.util.Objects;

/**
 * Base class for all domain entities.
 * Entities have identity and are compared by their ID.
 *
 * @param <ID> The type of the entity's identifier
 */
public abstract class Entity<ID> {

    /**
     * Returns the unique identifier of this entity.
     */
    public abstract ID getId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return getId() != null && Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : super.hashCode();
    }
}

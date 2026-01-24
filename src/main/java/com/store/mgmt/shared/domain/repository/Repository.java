package com.store.mgmt.shared.domain.repository;

import com.store.mgmt.shared.domain.model.AggregateRoot;

import java.util.Optional;

/**
 * Base repository interface for aggregates.
 * Repositories provide access to aggregates and handle persistence.
 *
 * @param <T>  The aggregate type
 * @param <ID> The aggregate's identifier type
 */
public interface Repository<T extends AggregateRoot<ID>, ID> {

    /**
     * Find an aggregate by its identifier.
     */
    Optional<T> findById(ID id);

    /**
     * Save an aggregate.
     */
    T save(T aggregate);

    /**
     * Delete an aggregate.
     */
    void delete(T aggregate);

    /**
     * Check if an aggregate with the given ID exists.
     */
    boolean existsById(ID id);
}

package com.store.mgmt.shared.domain.model;

import com.store.mgmt.shared.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots.
 * Aggregates are clusters of domain objects that are treated as a single unit.
 * The aggregate root is the entry point to the aggregate.
 *
 * @param <ID> The type of the aggregate's identifier
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Register a domain event to be published when the aggregate is saved.
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Get all uncommitted domain events.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clear all domain events after they have been published.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * Check if there are any uncommitted domain events.
     */
    public boolean hasEvents() {
        return !domainEvents.isEmpty();
    }
}

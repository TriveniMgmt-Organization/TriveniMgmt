package com.store.mgmt.shared.infrastructure.event;

import com.store.mgmt.shared.domain.event.DomainEvent;

import java.util.List;

/**
 * Interface for publishing domain events.
 */
public interface DomainEventPublisher {

    /**
     * Publish a single domain event.
     */
    void publish(DomainEvent event);

    /**
     * Publish multiple domain events.
     */
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
